package com.tastyhouse.domain.mail.port;

public interface MailSender {

    void send(String to, String subject, String content);
}
