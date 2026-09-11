package guessmarket.engine.exception.file;

// Represents an error that occurs while loading or validating a system file.
public class InvalidFileException extends Exception {
    private final FileErrorCode code;

    public InvalidFileException(FileErrorCode code, Object... parameters) {
        super(code.formatMessage(parameters));
        this.code = code;
    }

    public InvalidFileException(FileErrorCode code, Throwable cause, Object... parameters) {
        super(code.formatMessage(parameters), cause);
        this.code = code;
    }

    public FileErrorCode getCode() {
        return code;
    }
}
