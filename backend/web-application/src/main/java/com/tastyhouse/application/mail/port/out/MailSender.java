package com.tastyhouse.application.mail.port.out;

public interface MailSender {

    MailSendResult send(String to, String subject, String content);
}
