package ch.wipfli.microstreamclientplus.web.components.terminal;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import ch.wipfli.microstreamclientplus.web.models.Settings;
import ch.wipfli.microstreamclientplus.web.services.SettingsService;
import jdk.jshell.Diag;
import jdk.jshell.JShell;
import jdk.jshell.JShellException;
import jdk.jshell.SnippetEvent;
import jdk.jshell.SourceCodeAnalysis;

@Service
@SessionScope
public class JShellService {
    private final static String OUTPUT_START = "===========================START===========================";
    private final static String TRY_START = "try {\n";
    private final static String TRY_END = "}\ncatch(Exception e) {\nSystem.out.println(e);\n}";

    @Autowired
    private SettingsService settingsService;

    private JShell jShell;
    private ByteArrayOutputStream baos;

    private final List<String> classPath = new ArrayList<>();
    private final List<String> imports = new ArrayList<>();
    private final List<String> preCode = new ArrayList<>();
    private String databasePath;
    private String databaseRoot;

    @PostConstruct
    public void initialize() {
        final Settings settings = settingsService.findSettings();

        //databasePath
        this.databasePath = settings.getDatabasePath();

        //databaseRoot
        this.databaseRoot = settings.getDatabaseRootClass();

        //classpath
        this.classPath.addAll(Arrays.stream(System.getProperty("java.class.path").split(File.pathSeparator)).collect(Collectors.toList()));
        this.classPath.addAll(getDependenciesClassPath());
        this.classPath.addAll(settings.getClassPaths());

        //imports
        this.imports.add("import java.io.*;");
        this.imports.add("import java.math.*;");
        this.imports.add("import java.net.*;");
        this.imports.add("import java.nio.file.*;");
        this.imports.add("import java.util.*;");
        this.imports.add("import java.util.concurrent.*;");
        this.imports.add("import java.util.function.*;");
        this.imports.add("import java.util.prefs.*;");
        this.imports.add("import java.util.regex.*;");
        this.imports.add("import java.util.stream.*;");
        this.imports.add("import java.lang.reflect.*;");
        this.imports.add("import one.microstream.reflect.*;");
        this.imports.add("import one.microstream.storage.embedded.types.*;");
        this.imports.add("import static ch.wipfli.microstreamclientplus.web.Database.print;");
        this.imports.add("import static ch.wipfli.microstreamclientplus.web.Database.store;");
        this.imports.add("import static ch.wipfli.microstreamclientplus.web.Database.*;");
        this.imports.addAll(settings.getImports());

        //precode
        if (this.databaseRoot != null && this.databaseRoot.length() > 0 && this.databasePath != null && this.databasePath.length() > 0) {
            this.preCode.add("ch.wipfli.microstreamclientplus.web.Database.initialize(\"" + databaseRoot + "\", \"" + databasePath + "\");\n");
            this.preCode.add(
                "" + databaseRoot + " root() {\n" +
                    "" + databaseRoot + " r = (" + databaseRoot + ") ch.wipfli.microstreamclientplus.web.Database.root();\n" +
                    "return r;\n" +
                    "}\n");
        }

        //jshell
        this.baos = new ByteArrayOutputStream();
        final PrintStream printStream = new PrintStream(baos, true, StandardCharsets.UTF_8);
        this.jShell = buildShell(printStream);
    }

    public JShellService() {
        /*JShellConfiguration jShellConfiguration = new JShellConfiguration(
            "/home/fabian/microstream-spring-crud-store",
            "ch.wipfli.frontend.persistence.model.DataRoot",
            List.of("/home/fabian/Projects/Private/pksnb-svelte/frontend-wicket/target/classes"),
            List.of("import ch.wipfli.frontend.persistence.model.*;")
        );*/
    }

    public Result invokeeCode(String content) {
        final DynamicJavaCompiler javaCompiler = new DynamicJavaCompiler();

        final String code = String.join("\n", preCode) + "\n" + content;
        try {
            final DynamicJavaCompiler.Result result = javaCompiler.invoke(imports, code);

            final Result resultOriginal = new Result();
            for (DynamicJavaCompiler.Error item : result.getErrors()) {
                Error error = new Error();
                error.errorCode = item.getErrorCode();
                error.message = item.getMessage();
                error.endPosition = item.getEndPosition();
                error.startPosition = item.getStartPosition();
                resultOriginal.errors.add(error);
            }

            resultOriginal.output.addAll(resultOriginal.getOutput());

            return resultOriginal;
        }
        catch (RuntimeException e) {
            e.printStackTrace();
        }

        return new Result();
    }

    public Result invokeCode(String content) {
        final JShell jShell = this.jShell;

        final List<SnippetEvent> snippetEvents = new ArrayList<>();

        //imports
        snippetEvents.addAll(invokeSeq(jShell, this.imports));

        //database
        snippetEvents.addAll(invokeSeq(jShell, this.preCode));

        //start
        snippetEvents.addAll(jShell.eval("System.out.println(\"" + OUTPUT_START + "\");"));

        //add try catch
        content = TRY_START + content + TRY_END;

        //code
        snippetEvents.addAll(jShell.eval(content));

        snippetEvents.forEach(f -> jShell.drop(f.snippet()));
        final Result result = getResult(jShell, snippetEvents, baos);

        //remove snippets
        snippetEvents.forEach(f -> jShell.drop(f.snippet()));

        return result;
    }

    private Result getResult(JShell jShell, List<SnippetEvent> events, ByteArrayOutputStream baos) {
        final Result message = new Result();

        for (SnippetEvent snippetEvent : events) {
            final Stream<Diag> diagnostics = jShell.diagnostics(snippetEvent.snippet());
            JShellException shellException = snippetEvent.exception();
            if (shellException != null) {
                final Error r = new Error();
                r.setMessage(shellException.getClass().getName() + " " + shellException.getMessage());
                message.errors.add(r);
            }
            diagnostics.forEach(f ->
            {
                final Error r = new Error();
                r.setStartPosition(f.getStartPosition() - TRY_START.length());
                r.setEndPosition(f.getEndPosition() - TRY_START.length());
                r.setErrorCode(f.getCode());
                r.setMessage(f.getMessage(Locale.GERMAN));
                message.errors.add(r);
            });
        }

        String output = baos.toString(StandardCharsets.UTF_8);
        if (output.contains(OUTPUT_START)) {
            final int i = output.indexOf(OUTPUT_START);
            output = output.substring(i + OUTPUT_START.length() + 1);
        }
        message.getOutput().add(output);
        baos.reset();

        //this.classPath.forEach(f -> message.getOutput().add(f));
        return message;
    }

    private List<SnippetEvent> invokeSeq(JShell jShell, List<String> content) {
        final List<SnippetEvent> events = content.stream()
            .map(jShell::eval)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
        return events;
    }

    public List<String> suggest(String content) {
        final List<SnippetEvent> snippetEvents = new ArrayList<>();
        final JShell jShell = this.jShell;

        //imports
        snippetEvents.addAll(invokeSeq(jShell, this.imports));

        //database
        content = String.join("\n", preCode) + content;

        //invoke suggestion
        final List<SourceCodeAnalysis.Suggestion> completions = jShell.sourceCodeAnalysis().completionSuggestions(content, content.length(), new int[4]);

        //remove snippets
        snippetEvents.stream().map(SnippetEvent::snippet).forEach(jShell::drop);

        final List<String> results = completions.stream()
            .map(SourceCodeAnalysis.Suggestion::continuation)
            .collect(Collectors.toList());
        System.out.println(results);
        return results;
    }

    private JShell buildShell(PrintStream ps) {
        final JShell jShell = JShell.builder()
            .out(ps)
            .build();
        classPath.forEach(jShell::addToClasspath);
        return jShell;
    }

    private List<String> getDependenciesClassPath() {
        final List<String> result = new ArrayList<>();
        final String separator = System.getProperty("file.separator");
        final String parentFolder = "jar" + separator;

        final Optional<String> microstreamClientJarPath = this.classPath.stream()
            .filter(f -> f.contains(parentFolder))
            .findFirst();

        if (microstreamClientJarPath.isPresent()) {
            final int index = microstreamClientJarPath.get().indexOf(parentFolder);
            final String parentPath = microstreamClientJarPath.get().substring(0, index);
            final String dependencyPath = parentPath + "dependencies";
            result.add(dependencyPath);
            Optional.ofNullable(FileUtils.getFile(dependencyPath).listFiles()).stream()
                .filter(Objects::nonNull)
                .flatMap(Arrays::stream)
                .forEach(f -> result.add(f.getAbsolutePath()));
        }
        return result;
    }

    public static class Result {
        private final List<String> output = new ArrayList<>();
        private final List<Error> errors = new ArrayList<>();

        public List<String> getOutput() {
            return output;
        }

        public List<Error> getErrors() {
            return errors;
        }
    }

    public static class Error {
        private Long startPosition;
        private Long endPosition;
        private String errorCode;
        private String message;

        public Long getStartPosition() {
            return startPosition;
        }

        public void setStartPosition(Long startPosition) {
            this.startPosition = startPosition;
        }

        public Long getEndPosition() {
            return endPosition;
        }

        public void setEndPosition(Long endPosition) {
            this.endPosition = endPosition;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return "Result{" +
                "startPosition=" + startPosition +
                ", endPosition=" + endPosition +
                ", errorCode='" + errorCode + '\'' +
                ", message='" + message + '\'' +
                '}';
        }
    }
}
