package com.tastyhouse.application.auth.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.tastyhouse.application.auth.port.in.AdminAuthCommandUseCase;
import com.tastyhouse.application.auth.port.in.AdminAuthLoginCommand;
import com.tastyhouse.application.auth.port.out.AdminJwtResult;
import com.tastyhouse.application.auth.token.AdminTokenService;

@Service
@AdminApp
public class AdminAuthCommandService implements AdminAuthCommandUseCase {

    private final AuthenticationManager authenticationManager;
    private final AdminTokenService tokenService;

    public AdminAuthCommandService(AuthenticationManager authenticationManager, AdminTokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    @Override
    public AdminJwtResult login(AdminAuthLoginCommand command) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(command.username(), command.password())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return tokenService.issue(authentication, command.rememberMe());
    }

    @Override
    public AdminJwtResult refresh(String refreshToken) {
        return tokenService.refresh(refreshToken);
    }

    @Override
    public void logout(String bearerToken) {
        tokenService.revoke(bearerToken);
        SecurityContextHolder.clearContext();
    }
}
