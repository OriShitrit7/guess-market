package guessmarket.engine.exception;

import java.nio.file.Path;

public class NotXmlFileException extends InvalidFileException {
    private static final String MSG_FORMAT =
            "The file '%s' does not have the required '.xml' extension.";

    private final Path path;

    public NotXmlFileException(Path path) {
        this.path = path;
    }

    @Override
    public String getMessage() {
        Path fileName = path.getFileName();

        return String.format(MSG_FORMAT, fileName != null ? fileName : path);
    }
}
