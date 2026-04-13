package ak.ak32767.projecte;

import org.jetbrains.annotations.Nullable;

import java.security.KeyException;

public class ProjectEException {
    public static class YAMLKeyOrValueErrorException extends KeyException {
        public YAMLKeyOrValueErrorException() {
            super("key or value is invalid");
        }

        public YAMLKeyOrValueErrorException(@Nullable String reason) {
            super("key or value is invalid: " + reason);
        }

        public YAMLKeyOrValueErrorException(Exception e) {
            super("key or value is invalid: " + e);
        }
    }

}
