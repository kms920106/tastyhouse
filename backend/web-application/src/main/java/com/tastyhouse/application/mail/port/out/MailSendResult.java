package com.tastyhouse.application.mail.port.out;

public record MailSendResult(
    boolean success,
    Throwable cause
) {

    public static MailSendResult sent() {
        return new MailSendResult(true, null);
    }

    public static MailSendResult failed(Throwable cause) {
        return new MailSendResult(false, cause);
    }
}
