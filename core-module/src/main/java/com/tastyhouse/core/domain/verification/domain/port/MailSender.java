package com.tastyhouse.core.domain.verification.domain.port;

public interface MailSender {

    void send(String to, String subject, String content);
}
