package guessmarket.engine.exception;

import java.nio.file.Path;

public class FileReadFailedException extends InvalidFileException {
    private static final String MSG_FORMAT =
            "The file '%s' could not be read. Check that it is accessible, not locked, "
                    + "and that you have permission to read it.";

    private final Path path;

    public FileReadFailedException(Path path) {
        this.path = path;
    }

    @Override
    public String getMessage() {
        return String.format(MSG_FORMAT, path);
    }
}
