package com.tastyhouse.domain.sms.port;

public interface SmsSender {

    void send(String to, String content);
}
