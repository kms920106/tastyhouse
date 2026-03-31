package com.tastyhouse.webapi.auth;

import com.tastyhouse.core.entity.user.Gender;
import com.tastyhouse.webapi.auth.response.JwtResponse;
import com.tastyhouse.webapi.config.jwt.service.TokenService;
import com.tastyhouse.webapi.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final MemberService memberService;

    public void signUp(String username, String password,
                       String nickname, String fullName,
                       Gender gender, Integer birthDate, String phoneNumber,
                       Boolean pushNotificationEnabled,
                       Boolean marketingInfoEnabled, Boolean eventInfoEnabled,
                       String phoneVerifyToken, String emailVerifyToken,
                       String referrerNickname) {
        memberService.signUp(
            username, password, nickname, fullName, gender, birthDate, phoneNumber,
            pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled,
            phoneVerifyToken, emailVerifyToken, referrerNickname
        );
    }

    public JwtResponse login(String username, String password, boolean rememberMe) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(username, password)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return tokenService.issue(authentication, rememberMe);
    }

    public JwtResponse refresh(String refreshToken) {
        return tokenService.refresh(refreshToken);
    }

    public void logout(String bearerToken) {
        tokenService.revoke(bearerToken);
        SecurityContextHolder.clearContext();
    }
}
