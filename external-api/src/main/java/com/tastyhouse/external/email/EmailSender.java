package com.tastyhouse.external.email;

public interface EmailSender {

    void send(String to, String subject, String content);

    void sendHtml(String to, String subject, String htmlContent);
}
