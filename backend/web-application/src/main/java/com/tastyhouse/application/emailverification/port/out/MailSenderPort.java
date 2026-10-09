package com.tastyhouse.application.emailverification.port.out;

public interface MailSenderPort {

    MailSendResult send(String to, String subject, String content);
}
