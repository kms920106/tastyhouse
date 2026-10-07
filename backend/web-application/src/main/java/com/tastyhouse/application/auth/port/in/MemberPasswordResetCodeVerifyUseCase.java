package com.tastyhouse.application.auth.port.in;

public interface MemberPasswordResetCodeVerifyUseCase {

    String verifyPasswordResetCode(String username, String verificationCode);
}
