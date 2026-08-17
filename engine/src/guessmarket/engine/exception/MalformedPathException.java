package guessmarket.engine.exception;

public class MalformedPathException extends InvalidFileException {
    private static final String MSG_FORMAT =
            "The file path is not valid on this system: '%s'.";

    private final String pathText;

    public MalformedPathException(String pathText) {
        this.pathText = pathText;
    }

    @Override
    public String getMessage() {
        return String.format(MSG_FORMAT, pathText);
    }
}
