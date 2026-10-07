package com.tastyhouse.application.auth.service;

import org.springframework.stereotype.Service;

import com.tastyhouse.application.auth.port.in.AdminTokenRefreshUseCase;
import com.tastyhouse.application.auth.port.out.AdminJwtResult;
import com.tastyhouse.application.auth.token.AdminTokenService;

@Service
class AdminTokenRefreshService implements AdminTokenRefreshUseCase {

    private final AdminTokenService tokenService;

    public AdminTokenRefreshService(AdminTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public AdminJwtResult refresh(String refreshToken) {
        return tokenService.refresh(refreshToken);
    }
}
