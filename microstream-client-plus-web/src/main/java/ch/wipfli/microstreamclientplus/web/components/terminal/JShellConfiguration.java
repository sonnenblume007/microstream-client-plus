package ch.wipfli.microstreamclientplus.web.components.terminal;

import java.io.Serializable;
import java.util.List;

public class JShellConfiguration implements Serializable {

    private final String databasePath;
    private final String databaseRoot;
    private final List<String> classPath;
    private final List<String> imports;

    public JShellConfiguration(String databasePath, String databaseRoot, List<String> classPath, List<String> imports) {
        this.databasePath = databasePath;
        this.databaseRoot = databaseRoot;
        this.classPath = classPath;
        this.imports = imports;
    }

    public String getDatabasePath() {
        return databasePath;
    }

    public String getDatabaseRoot() {
        return databaseRoot;
    }

    public List<String> getClassPath() {
        return classPath;
    }

    public List<String> getImports() {
        return imports;
    }
}
