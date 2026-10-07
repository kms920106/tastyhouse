package com.tastyhouse.application.auth.service;

import org.springframework.stereotype.Service;

import com.tastyhouse.application.auth.port.in.MemberLogoutUseCase;

@Service
class MemberLogoutService implements MemberLogoutUseCase {

    private final CredentialLoginService credentialLoginService;

    public MemberLogoutService(CredentialLoginService credentialLoginService) {
        this.credentialLoginService = credentialLoginService;
    }

    @Override
    public void logout(String bearerToken) {
        credentialLoginService.logout(bearerToken);
    }
}
