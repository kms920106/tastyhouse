package com.tastyhouse.application.auth.service;

import org.springframework.stereotype.Service;

import com.tastyhouse.application.auth.port.in.MemberPasswordResetCodeSendUseCase;

@Service
class MemberPasswordResetCodeSendService implements MemberPasswordResetCodeSendUseCase {

    private final AuthPasswordResetService authPasswordResetService;

    public MemberPasswordResetCodeSendService(AuthPasswordResetService authPasswordResetService) {
        this.authPasswordResetService = authPasswordResetService;
    }

    @Override
    public void sendPasswordResetCode(String username) {
        authPasswordResetService.sendPasswordResetCode(username);
    }
}
