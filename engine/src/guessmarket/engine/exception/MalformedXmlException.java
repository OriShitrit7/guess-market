package guessmarket.engine.exception;

import java.nio.file.Path;

// Reports malformed XML and includes parser location details when they are available.
public class MalformedXmlException extends InvalidFileException {
    private static final String MSG_FORMAT =
            "The file '%s' is not a well-formed XML document.";
    private static final String LOCATION_FORMAT =
            "%s%nError at line %d, column %d: %s";

    private final Path path;
    private final int lineNumber;
    private final int columnNumber;
    private final String reason;

    public MalformedXmlException(Path path) {
        this(path, 0, 0, null);
    }

    public MalformedXmlException(Path path, int lineNumber, int columnNumber, String reason) {
        this.path = path;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
        this.reason = reason;
    }

    @Override
    public String getMessage() {
        String message = String.format(MSG_FORMAT, path.getFileName());

        if (reason == null) {
            return message;
        }
        return String.format(LOCATION_FORMAT, message, lineNumber, columnNumber, reason);
    }
}
