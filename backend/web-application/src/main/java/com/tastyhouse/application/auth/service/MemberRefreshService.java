package com.tastyhouse.application.auth.service;

import org.springframework.stereotype.Service;

import com.tastyhouse.application.auth.port.in.MemberRefreshUseCase;
import com.tastyhouse.application.auth.port.out.MemberJwtResult;

@Service
class MemberRefreshService implements MemberRefreshUseCase {

    private final CredentialLoginService credentialLoginService;

    public MemberRefreshService(CredentialLoginService credentialLoginService) {
        this.credentialLoginService = credentialLoginService;
    }

    @Override
    public MemberJwtResult refresh(String refreshToken) {
        return credentialLoginService.refresh(refreshToken);
    }
}
