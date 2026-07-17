package com.tastyhouse.webapi.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.tastyhouse.core.domain.member.domain.model.MemberGender;
import com.tastyhouse.core.domain.member.domain.model.MemberSocialProvider;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.webapi.auth.apple.AppleSocialLoginService;
import com.tastyhouse.webapi.auth.facebook.FacebookSocialLoginService;
import com.tastyhouse.webapi.auth.kakao.KakaoSocialLoginService;
import com.tastyhouse.webapi.auth.naver.NaverSocialLoginService;
import com.tastyhouse.webapi.auth.response.JwtResponse;
import com.tastyhouse.webapi.auth.response.PasswordResetTokenResponse;
import com.tastyhouse.webapi.auth.response.PhoneLoginResponse;
import com.tastyhouse.webapi.auth.response.SocialLinkResponse;
import com.tastyhouse.webapi.auth.response.SocialLoginResponse;
import com.tastyhouse.webapi.auth.service.AuthPasswordResetService;
import com.tastyhouse.webapi.auth.service.AuthService;
import com.tastyhouse.webapi.auth.service.PhoneLoginService;

@Component
@RequiredArgsConstructor
public class AuthFacade {

    private final AuthService authService;
    private final AuthPasswordResetService authPasswordResetService;
    private final KakaoSocialLoginService kakaoSocialLoginService;
    private final NaverSocialLoginService naverSocialLoginService;
    private final FacebookSocialLoginService facebookSocialLoginService;
    private final AppleSocialLoginService appleSocialLoginService;
    private final PhoneLoginService phoneLoginService;

    // 회원가입
    public void signUp(String username, String password,
                       String nickname, String fullName,
                       String gender, Integer birthDate, String phoneNumber,
                       boolean pushNotificationEnabled,
                       boolean marketingInfoEnabled, boolean eventInfoEnabled,
                       String phoneVerifyToken, String emailVerifyToken,
                       String referrerNickname) {
        authService.signUp(
            username, password, nickname, fullName, MemberGender.from(gender), birthDate, phoneNumber,
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
    public SocialLoginResponse kakaoLogin(String authorizationCode) {
        return kakaoSocialLoginService.login(authorizationCode);
    }

    // 휴대폰 인증 토큰으로 로그인 (기존 회원이면 JWT 발급, 신규이면 needsSignUp=true)
    public PhoneLoginResponse phoneLogin(String phoneVerifyToken) {
        return phoneLoginService.login(phoneVerifyToken);
    }

    // 소셜 계정을 기존 일반가입 계정에 연동 후 JWT 발급. 가입된 계정이 없으면 NEEDS_SIGN_UP 반환
    public SocialLinkResponse linkAccount(String provider, String tempToken, String phoneVerifyToken) {
        return switch (MemberSocialProvider.from(provider)) {
            case KAKAO -> kakaoSocialLoginService.linkAccount(tempToken, phoneVerifyToken);
            case NAVER -> naverSocialLoginService.linkAccount(tempToken, phoneVerifyToken);
            case FACEBOOK -> facebookSocialLoginService.linkAccount(tempToken, phoneVerifyToken);
            case APPLE -> appleSocialLoginService.linkAccount(tempToken, phoneVerifyToken);
            default -> throw new BusinessException(ErrorCode.SOCIAL_PROVIDER_TYPE_UNKNOWN,
                ErrorCode.SOCIAL_PROVIDER_TYPE_UNKNOWN.getDefaultMessage() + ": " + provider);
        };
    }

    // 네이버 로그인 (기존 회원이면 JWT 발급, 신규이면 회원가입 필요 응답)
    public SocialLoginResponse naverLogin(String authorizationCode, String state) {
        return naverSocialLoginService.login(authorizationCode, state);
    }

    // 페이스북 로그인 (기존 회원이면 JWT 발급, 신규이면 회원가입 필요 응답)
    public SocialLoginResponse facebookLogin(String accessToken) {
        return facebookSocialLoginService.login(accessToken);
    }

    // 애플 로그인 (기존 회원이면 JWT 발급, 신규이면 회원가입 필요 응답)
    public SocialLoginResponse appleLogin(String authorizationCode) {
        return appleSocialLoginService.login(authorizationCode);
    }

    // 소셜 소셜 회원가입 후 JWT 발급
    public JwtResponse socialSignUp(String provider, String tempToken, String username, String nickname,
                                    String fullName, String gender, Integer birthDate, String phoneNumber,
                                    boolean pushNotificationEnabled, boolean marketingInfoEnabled,
                                    boolean eventInfoEnabled, String referrerNickname) {
        MemberGender genderType = MemberGender.from(gender);
        return switch (MemberSocialProvider.from(provider)) {
            case KAKAO -> kakaoSocialLoginService.signUp(
                tempToken, username, nickname, fullName, genderType, birthDate, phoneNumber,
                pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled, referrerNickname
            );
            case NAVER -> naverSocialLoginService.signUp(
                tempToken, username, nickname, fullName, genderType, birthDate, phoneNumber,
                pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled, referrerNickname
            );
            case FACEBOOK -> facebookSocialLoginService.signUp(
                tempToken, username, nickname, fullName, genderType, birthDate, phoneNumber,
                pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled, referrerNickname
            );
            case APPLE -> appleSocialLoginService.signUp(
                tempToken, username, nickname, fullName, genderType, birthDate, phoneNumber,
                pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled, referrerNickname
            );
            default -> throw new BusinessException(ErrorCode.SOCIAL_PROVIDER_TYPE_UNKNOWN,
                ErrorCode.SOCIAL_PROVIDER_TYPE_UNKNOWN.getDefaultMessage() + ": " + provider);
        };
    }
}
