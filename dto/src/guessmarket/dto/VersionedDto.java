package guessmarket.dto;

// Wraps data sent to a client with the version of that data, so the next request can ask only for what is new.
public final class VersionedDto<T> {
    private final T data;
    private final int version;

    public VersionedDto(T data, int version) {
        this.data = data;
        this.version = version;
    }

    public T getData() {
        return data;
    }

    public int getVersion() {
        return version;
    }
}
