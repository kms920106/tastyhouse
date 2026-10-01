package com.tastyhouse.application.file.port.out;

public record FileDeleteResult(
    boolean success,
    Throwable cause
) {

    public static FileDeleteResult deleted() {
        return new FileDeleteResult(true, null);
    }

    public static FileDeleteResult failed(Throwable cause) {
        return new FileDeleteResult(false, cause);
    }
}
