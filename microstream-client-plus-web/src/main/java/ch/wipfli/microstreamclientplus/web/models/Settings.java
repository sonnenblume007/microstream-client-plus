package ch.wipfli.microstreamclientplus.web.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.FieldNameConstants;

@Data
@FieldNameConstants
public class Settings implements Serializable {
    private List<String> imports = new ArrayList<>();
    private List<String> classPaths = new ArrayList<>();
    private String databaseRootClass = "";
    private String databasePath = "";
}
