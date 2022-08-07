package ch.wipfli.microstreamclientplus.web.components.terminal;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;


public class DynamicJavaCompiler {

    private final static String className = "MyClass";

    public static void main(String[] args) throws Exception {
        final List<String> imports = new ArrayList<>();
        imports.add("import java.io.*;");
        imports.add("import java.math.*;");
        imports.add("import java.net.*;");
        imports.add("import java.nio.file.*;");
        imports.add("import java.util.*;");
        imports.add("import java.util.concurrent.*;");
        imports.add("import java.util.function.*;");
        imports.add("import java.util.prefs.*;");
        imports.add("import java.util.regex.*;");
        imports.add("import java.util.stream.*;");
        imports.add("import java.lang.reflect.*;");
        imports.add("import one.microstream.reflect.*;");
        imports.add("import one.microstream.storage.embedded.types.*;");
        imports.add("import ch.wipfli.frontend.persistence.model.*;");

        final String content = "" +
            "DataRoot root  = new DataRoot();\n" +
            "EmbeddedStorageManager storageManager = EmbeddedStorage.Foundation(Paths.get(\"/home/fabian/microstream-spring-crud-store\"))\n" +
            "                .onConnectionFoundation(cf -> cf.setClassLoaderProvider(ClassLoaderProvider.New(Thread.currentThread().getContextClassLoader())))\n" +
            "                .start(root);\n" +
            "                \n" +
            "                \n" +
            "DataRoot root  = new DataRoot();\n" +
            "EmbeddedStorageManager storageManager = EmbeddedStorage.start(\n" +
            "    root,             // root object\n" +
            "    Paths.get(\"/home/fabian/microstream-spring-crud-store\") \n" +
            ");";

        final List<String> classpathEntries = Arrays.stream(System.getProperty("java.class.path").split(File.pathSeparator))
            .collect(Collectors.toList());
        //addToClasspath(new File("/home/fabian/Projects/Private/pksnb-svelte/frontend-wicket/target/classes"));
        //classpathEntries.forEach(f->addToClasspath(new File(f)));
        final DynamicJavaCompiler javaCompiler = new DynamicJavaCompiler();
        final Result result = new Result();
        final String javaContent = javaCompiler.createRunnableCode(new ArrayList<>(), content);
        final Path path = javaCompiler.createFile(javaContent);
        javaCompiler.compile(path, result);
        for (Error err : result.errors) {
            System.out.println(err.message);
        }
        if (result.errors.isEmpty()) {
            javaCompiler.run(path);
        }
    }

    public Result invoke(List<String> imports, String content) {
        final Result result = new Result();
        final String javaContent = createRunnableCode(imports, content);
        try {
            final List<String> classpathEntriesOld = Arrays.stream(System.getProperty("java.class.path").split(File.pathSeparator))
                .collect(Collectors.toList());
            //addToClasspath(new File("/home/fabian/Projects/Private/pksnb-svelte/frontend-wicket/target/classes"));
            final List<String> classpathEntriesNew = Arrays.stream(System.getProperty("java.class.path").split(File.pathSeparator))
                .collect(Collectors.toList());
            final Path path = createFile(javaContent);
            compile(path, result);
            if (result.errors.isEmpty()) {
                run(path);
            }
        }
        catch (Exception e) {
            final Error error = new Error();
            error.message = e.getMessage();
            result.getErrors().add(error);
        }
        return result;
    }

    private void run(Path javaSourcePath) throws Exception {
        final ClassLoader classLoader = DynamicJavaCompiler.class.getClassLoader();
        final URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{javaSourcePath.getParent().getParent().getParent().toUri().toURL()}, classLoader);
        final Class<?> javaDemoClass = urlClassLoader.loadClass("ch.terminal." + className);
        final Method method = javaDemoClass.getMethod("run");
        method.invoke(null);
    }

    private void compile(final Path javaSourcePath, Result result) {

        // Get the compiler

        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        // Get the file system manager of the compiler
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null)) {
            // Create a compilation unit (files)
            final Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(List.of(javaSourcePath.toFile()));
            // A feedback object (diagnostic) to get errors
            final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            // Compilation unit can be created and called only once
            final JavaCompiler.CompilationTask task = compiler.getTask(
                null,
                fileManager,
                diagnostics,
                null,
                null,
                compilationUnits
            );
            // The compile task is called
            task.call();
            // Printing of any compile problems
            for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
                Error error = new Error();
                error.errorCode = diagnostic.getCode();
                error.message = diagnostic.getMessage(Locale.GERMAN);
                error.startPosition = diagnostic.getStartPosition();
                error.endPosition = diagnostic.getEndPosition();
                result.errors.add(error);
            }
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private Path createFile(String content) throws IOException {
        final Path temp = Paths.get(System.getProperty("java.io.tmpdir"), "dynamicJavaCompliler", UUID.randomUUID().toString() + "/ch" + "/terminal");
        Files.createDirectories(temp);
        final Path javaSourceFile = Paths.get(temp.normalize().toAbsolutePath().toString(), className + ".java");
        Files.write(javaSourceFile, content.getBytes());

        System.out.println(javaSourceFile.toAbsolutePath().toString());
        return javaSourceFile;
    }

    private String createRunnableCode(List<String> imports, String content) {
        final StringBuilder result = new StringBuilder();

        result.append("package ch.terminal;").append("\n");
        imports.forEach(f -> result.append(f).append("\n"));
        String code = "" +
            "public class " + className + " {\n" +
            "public static void run() {\n" +
            "    " + content + "\n" +
            "    }\n" +
            "}\n";

        result.append(code);
        return result.toString();
    }

    public static void addToClasspath(File file) {
        try {
            URL u = file.toURL();
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            URLClassLoader urlClassLoader = URLClassLoader.newInstance(new URL[]{u}, classLoader);
            Thread.currentThread().setContextClassLoader(urlClassLoader);
            Class urlClass = URLClassLoader.class;
            Method method = urlClass.getDeclaredMethod("addURL", new Class[]{URL.class});
            method.setAccessible(true);
            method.invoke(urlClassLoader, new Object[]{u});
        }
        catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
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
