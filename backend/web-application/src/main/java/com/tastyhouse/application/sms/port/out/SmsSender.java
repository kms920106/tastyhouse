package com.tastyhouse.application.sms.port.out;

public interface SmsSender {

    SmsSendResult send(String to, String content);
}
