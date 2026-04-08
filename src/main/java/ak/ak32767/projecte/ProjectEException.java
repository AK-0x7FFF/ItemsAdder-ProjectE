package ak.ak32767.projecte;

import java.security.KeyException;

public class ProjectEException {
    public static class YAMLMaterialNotFoundException extends RuntimeException {
        public YAMLMaterialNotFoundException() {
            super("Material not found");
        }
    }

    public static class YAMLTagNotFoundException extends RuntimeException {
        public YAMLTagNotFoundException() {
            super("Tag not found");
        }
    }

    public static class YAMLKeyValueErrorException extends KeyException {
        public YAMLKeyValueErrorException() {
            super("key or value is invalid");
        }
        }

}
