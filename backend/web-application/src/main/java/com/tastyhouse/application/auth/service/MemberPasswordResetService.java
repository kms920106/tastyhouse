package com.tastyhouse.application.auth.service;

import org.springframework.stereotype.Service;

import com.tastyhouse.application.auth.port.in.MemberPasswordResetUseCase;

@Service
class MemberPasswordResetService implements MemberPasswordResetUseCase {

    private final AuthPasswordResetService authPasswordResetService;

    public MemberPasswordResetService(AuthPasswordResetService authPasswordResetService) {
        this.authPasswordResetService = authPasswordResetService;
    }

    @Override
    public void resetPassword(String passwordResetToken, String newPassword, String newPasswordConfirm) {
        authPasswordResetService.resetPassword(passwordResetToken, newPassword, newPasswordConfirm);
    }
}
