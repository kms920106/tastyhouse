package com.tastyhouse.application.auth.service;

import org.springframework.stereotype.Service;

import com.tastyhouse.application.auth.port.in.MemberPasswordResetCodeVerifyUseCase;

@Service
class MemberPasswordResetCodeVerifyService implements MemberPasswordResetCodeVerifyUseCase {

    private final AuthPasswordResetService authPasswordResetService;

    public MemberPasswordResetCodeVerifyService(AuthPasswordResetService authPasswordResetService) {
        this.authPasswordResetService = authPasswordResetService;
    }

    @Override
    public String verifyPasswordResetCode(String username, String verificationCode) {
        return authPasswordResetService.verifyPasswordResetCode(username, verificationCode);
    }
}
