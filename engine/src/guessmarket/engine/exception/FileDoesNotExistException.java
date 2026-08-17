package guessmarket.engine.exception;

import java.nio.file.Path;

public class FileDoesNotExistException extends InvalidFileException {
    private static final String MSG_FORMAT = "The file does not exist: '%s'.";

    private final Path path;

    public FileDoesNotExistException(Path path) {
        this.path = path;
    }

    @Override
    public String getMessage() {
        return String.format(MSG_FORMAT, path);
    }
}
