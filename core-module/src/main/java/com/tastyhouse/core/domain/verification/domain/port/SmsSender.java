package com.tastyhouse.core.domain.verification.domain.port;

public interface SmsSender {

    void send(String to, String content);
}
