package org.gaussian.vgraph.exception;

public class DeserializationException extends RuntimeException{
    private final byte[] bytes;

    public DeserializationException(String message, byte[] bytes, Throwable cause) {
        super(message, cause);
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }
}
