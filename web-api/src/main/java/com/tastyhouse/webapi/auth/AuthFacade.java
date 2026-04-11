package com.tastyhouse.webapi.auth;

import com.tastyhouse.core.entity.user.Gender;
import com.tastyhouse.webapi.auth.kakao.KakaoSocialLoginService;
import com.tastyhouse.webapi.auth.response.JwtResponse;
import com.tastyhouse.webapi.auth.response.KakaoLinkResponse;
import com.tastyhouse.webapi.auth.response.KakaoLoginResponse;
import com.tastyhouse.webapi.auth.response.PasswordResetTokenResponse;
import com.tastyhouse.webapi.auth.response.PhoneLoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthFacade {

    private final AuthService authService;
    private final AuthPasswordResetService authPasswordResetService;
    private final KakaoSocialLoginService kakaoSocialLoginService;
    private final PhoneLoginService phoneLoginService;

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

    // 카카오 로그인 (기존 회원이면 JWT 발급, 신규이면 회원가입 필요 응답)
    public KakaoLoginResponse kakaoLogin(String authorizationCode) {
        return kakaoSocialLoginService.login(authorizationCode);
    }

    // 휴대폰 인증 토큰으로 로그인 (기존 회원이면 JWT 발급, 신규이면 needsSignUp=true)
    public PhoneLoginResponse phoneLogin(String phoneVerifyToken) {
        return phoneLoginService.login(phoneVerifyToken);
    }

    // 카카오 계정을 기존 일반가입 계정에 연동 후 JWT 발급. 가입된 계정이 없으면 NEEDS_SIGN_UP 반환
    public KakaoLinkResponse kakaoLinkAccount(String kakaoTempToken, String phoneVerifyToken) {
        return kakaoSocialLoginService.linkAccount(kakaoTempToken, phoneVerifyToken);
    }

    // 카카오 소셜 회원가입 후 JWT 발급
    public JwtResponse kakaoSignUp(String kakaoTempToken, String username, String nickname, String fullName,
                                   Gender gender, Integer birthDate, String phoneNumber,
                                   Boolean pushNotificationEnabled, Boolean marketingInfoEnabled,
                                   Boolean eventInfoEnabled, String referrerNickname) {
        return kakaoSocialLoginService.signUp(
            kakaoTempToken, username, nickname, fullName, gender, birthDate, phoneNumber,
            pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled, referrerNickname
        );
    }
}
