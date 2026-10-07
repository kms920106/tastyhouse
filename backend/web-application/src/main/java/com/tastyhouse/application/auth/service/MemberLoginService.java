package com.tastyhouse.application.auth.service;

import org.springframework.stereotype.Service;

import com.tastyhouse.application.auth.port.in.MemberLoginUseCase;
import com.tastyhouse.application.auth.port.out.MemberJwtResult;

@Service
class MemberLoginService implements MemberLoginUseCase {

    private final CredentialLoginService credentialLoginService;

    public MemberLoginService(CredentialLoginService credentialLoginService) {
        this.credentialLoginService = credentialLoginService;
    }

    @Override
    public MemberJwtResult login(String username, String password, boolean rememberMe) {
        return credentialLoginService.login(username, password, rememberMe);
    }
}
