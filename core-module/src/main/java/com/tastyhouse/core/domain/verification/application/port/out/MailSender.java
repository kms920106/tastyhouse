package com.tastyhouse.core.domain.verification.application.port.out;

public interface MailSender {

    void send(String to, String subject, String content);

    void sendHtml(String to, String subject, String htmlContent);
}
