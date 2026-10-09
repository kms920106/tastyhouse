package com.tastyhouse.application.phoneverification.port.out;

public record SmsSendResult(
    SmsSendFailure failure,
    Throwable cause
) {

    public static SmsSendResult sent() {
        return new SmsSendResult(null, null);
    }

    public static SmsSendResult failed(SmsSendFailure failure) {
        return new SmsSendResult(failure, null);
    }

    public static SmsSendResult failed(SmsSendFailure failure, Throwable cause) {
        return new SmsSendResult(failure, cause);
    }

    public boolean success() {
        return failure == null;
    }
}
