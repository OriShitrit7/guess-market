package guessmarket.engine.exception;

import java.nio.file.Path;

public class NotAFileException extends InvalidFileException {
    private static final String MSG_FORMAT = "The path does not point to a regular file: '%s'.";

    private final Path path;

    public NotAFileException(Path path) {
        this.path = path;
    }

    @Override
    public String getMessage() {
        return String.format(MSG_FORMAT, path);
    }

}
