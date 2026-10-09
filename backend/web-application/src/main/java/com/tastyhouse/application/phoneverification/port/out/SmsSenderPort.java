package com.tastyhouse.application.phoneverification.port.out;

public interface SmsSenderPort {

    SmsSendResult send(String to, String content);
}
