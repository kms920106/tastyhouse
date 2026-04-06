package com.tastyhouse.webapi.auth;

import com.tastyhouse.core.entity.user.Gender;
import com.tastyhouse.webapi.auth.response.JwtResponse;
import com.tastyhouse.webapi.auth.response.PasswordResetTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthFacade {

    private final AuthService authService;
    private final AuthPasswordResetService authPasswordResetService;

    // 회원가입
    public void signUp(String username, String password,
                       String nickname, String fullName,
                       Gender gender, Integer birthDate, String phoneNumber,
                       Boolean pushNotificationEnabled,
                       Boolean marketingInfoEnabled, Boolean eventInfoEnabled,
                       String phoneVerifyToken, String emailVerifyToken,
                       String referrerNickname) {
        authService.signUp(
            username, password, nickname, fullName, gender, birthDate, phoneNumber,
            pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled,
            phoneVerifyToken, emailVerifyToken, referrerNickname
        );
    }

    // 로그인
    public JwtResponse login(String username, String password, boolean rememberMe) {
        return authService.login(username, password, rememberMe);
    }

    // 토큰 갱신
    public JwtResponse refresh(String refreshToken) {
        return authService.refresh(refreshToken);
    }

    // 로그아웃
    public void logout(String bearerToken) {
        authService.logout(bearerToken);
    }

    // 비밀번호 찾기 - 인증코드 발송
    public void sendPasswordResetCode(String username) {
        authPasswordResetService.sendPasswordResetCode(username);
    }

    // 비밀번호 찾기 - 인증코드 확인
    public PasswordResetTokenResponse verifyPasswordResetCode(String username, String verificationCode) {
        return authPasswordResetService.verifyPasswordResetCode(username, verificationCode);
    }

    // 비밀번호 재설정
    public void resetPassword(String passwordResetToken, String newPassword, String newPasswordConfirm) {
        authPasswordResetService.resetPassword(passwordResetToken, newPassword, newPasswordConfirm);
    }
}
