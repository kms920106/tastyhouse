package com.tastyhouse.application.auth.service;

import org.springframework.stereotype.Service;

import com.tastyhouse.application.auth.port.in.CeoTokenRefreshUseCase;
import com.tastyhouse.application.auth.port.out.CeoJwtResult;
import com.tastyhouse.application.auth.token.CeoTokenService;

@Service
class CeoTokenRefreshService implements CeoTokenRefreshUseCase {

    private final CeoTokenService tokenService;

    public CeoTokenRefreshService(CeoTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public CeoJwtResult refresh(String refreshToken) {
        return tokenService.refresh(refreshToken);
    }
}
