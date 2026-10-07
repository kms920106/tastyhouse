package com.tastyhouse.application.auth.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.tastyhouse.application.auth.port.in.CeoLogoutUseCase;
import com.tastyhouse.application.auth.token.CeoTokenService;

@Service
class CeoLogoutService implements CeoLogoutUseCase {

    private final CeoTokenService tokenService;

    public CeoLogoutService(CeoTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public void logout(String bearerToken) {
        tokenService.revoke(bearerToken);
        SecurityContextHolder.clearContext();
    }
}
