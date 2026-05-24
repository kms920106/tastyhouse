package com.tastyhouse.core.domain.verification.application.port.out;

public interface SmsSender {

    void send(String to, String content);
}
