package com.tastyhouse.application.auth.port.in;

public interface MemberPasswordResetUseCase {

    void resetPassword(String passwordResetToken, String newPassword, String newPasswordConfirm);
}
