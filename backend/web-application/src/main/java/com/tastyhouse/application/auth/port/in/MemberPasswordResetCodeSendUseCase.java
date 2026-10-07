package com.tastyhouse.application.auth.port.in;

public interface MemberPasswordResetCodeSendUseCase {

    void sendPasswordResetCode(String username);
}
