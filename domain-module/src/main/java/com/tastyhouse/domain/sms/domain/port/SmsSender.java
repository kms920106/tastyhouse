package com.tastyhouse.domain.sms.domain.port;

public interface SmsSender {

    void send(String to, String content);
}
