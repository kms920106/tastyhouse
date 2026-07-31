package com.tastyhouse.domain.mail.domain.port;

public interface MailSender {

    void send(String to, String subject, String content);
}
