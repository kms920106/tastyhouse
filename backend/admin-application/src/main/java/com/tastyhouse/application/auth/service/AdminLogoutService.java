package com.tastyhouse.application.auth.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.tastyhouse.application.auth.port.in.AdminLogoutUseCase;
import com.tastyhouse.application.auth.token.AdminTokenService;

@Service
class AdminLogoutService implements AdminLogoutUseCase {

    private final AdminTokenService tokenService;

    public AdminLogoutService(AdminTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public void logout(String bearerToken) {
        tokenService.revoke(bearerToken);
        SecurityContextHolder.clearContext();
    }
}
