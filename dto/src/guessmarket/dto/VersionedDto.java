package guessmarket.dto;

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
