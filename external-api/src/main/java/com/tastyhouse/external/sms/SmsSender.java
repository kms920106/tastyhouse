package com.tastyhouse.external.sms;

public interface SmsSender {

    void send(String to, String content);
}
