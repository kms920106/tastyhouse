package com.tastyhouse.application.auth.service;

import com.tastyhouse.application.shared.marker.WebApp;
import org.springframework.stereotype.Service;

import com.tastyhouse.domain.member.model.MemberGender;
import com.tastyhouse.domain.member.model.MemberSocialProvider;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.application.auth.service.apple.AppleSocialLoginService;
import com.tastyhouse.application.auth.service.facebook.FacebookSocialLoginService;
import com.tastyhouse.application.auth.service.kakao.KakaoSocialLoginService;
import com.tastyhouse.application.auth.service.naver.NaverSocialLoginService;
import com.tastyhouse.application.auth.port.in.MemberAuthCommandUseCase;
import com.tastyhouse.application.auth.port.in.AuthSignUpCommand;
import com.tastyhouse.application.auth.port.in.AuthSocialSignUpCommand;
import com.tastyhouse.application.auth.port.out.MemberJwtResult;
import com.tastyhouse.application.auth.port.out.PhoneLoginResult;
import com.tastyhouse.application.auth.port.out.SocialLinkResult;
import com.tastyhouse.application.auth.port.out.SocialLoginResult;

@Service
@WebApp
public class MemberAuthCommandService implements MemberAuthCommandUseCase {

    private final CredentialLoginService credentialLoginService;
    private final AuthPasswordResetService authPasswordResetService;
    private final KakaoSocialLoginService kakaoSocialLoginService;
    private final NaverSocialLoginService naverSocialLoginService;
    private final FacebookSocialLoginService facebookSocialLoginService;
    private final AppleSocialLoginService appleSocialLoginService;
    private final PhoneLoginService phoneLoginService;

    public MemberAuthCommandService(
        CredentialLoginService credentialLoginService,
        AuthPasswordResetService authPasswordResetService,
        KakaoSocialLoginService kakaoSocialLoginService,
        NaverSocialLoginService naverSocialLoginService,
        FacebookSocialLoginService facebookSocialLoginService,
        AppleSocialLoginService appleSocialLoginService,
        PhoneLoginService phoneLoginService
    ) {
        this.credentialLoginService = credentialLoginService;
        this.authPasswordResetService = authPasswordResetService;
        this.kakaoSocialLoginService = kakaoSocialLoginService;
        this.naverSocialLoginService = naverSocialLoginService;
        this.facebookSocialLoginService = facebookSocialLoginService;
        this.appleSocialLoginService = appleSocialLoginService;
        this.phoneLoginService = phoneLoginService;
    }

    @Override
    public Long signUp(AuthSignUpCommand command) {
        return credentialLoginService.signUp(
            command.username(), command.password(), command.nickname(), command.fullName(),
            MemberGender.from(command.gender()), command.birthDate(), command.phoneNumber(),
            command.pushNotificationEnabled(), command.marketingInfoEnabled(), command.eventInfoEnabled(),
            command.smsVerifyToken(), command.mailVerifyToken(), command.referrerNickname()
        );
    }

    @Override
    public MemberJwtResult login(String username, String password, boolean rememberMe) {
        return credentialLoginService.login(username, password, rememberMe);
    }

    @Override
    public MemberJwtResult refresh(String refreshToken) {
        return credentialLoginService.refresh(refreshToken);
    }

    @Override
    public void logout(String bearerToken) {
        credentialLoginService.logout(bearerToken);
    }

    @Override
    public void sendPasswordResetCode(String username) {
        authPasswordResetService.sendPasswordResetCode(username);
    }

    @Override
    public String verifyPasswordResetCode(String username, String verificationCode) {
        return authPasswordResetService.verifyPasswordResetCode(username, verificationCode);
    }

    @Override
    public void resetPassword(String passwordResetToken, String newPassword, String newPasswordConfirm) {
        authPasswordResetService.resetPassword(passwordResetToken, newPassword, newPasswordConfirm);
    }

    @Override
    public SocialLoginResult kakaoLogin(String authorizationCode) {
        return kakaoSocialLoginService.login(authorizationCode);
    }

    @Override
    public PhoneLoginResult phoneLogin(String smsVerifyToken) {
        return phoneLoginService.login(smsVerifyToken);
    }

    @Override
    public SocialLinkResult linkAccount(String provider, String tempToken, String smsVerifyToken) {
        return switch (MemberSocialProvider.from(provider)) {
            case KAKAO -> kakaoSocialLoginService.linkAccount(tempToken, smsVerifyToken);
            case NAVER -> naverSocialLoginService.linkAccount(tempToken, smsVerifyToken);
            case FACEBOOK -> facebookSocialLoginService.linkAccount(tempToken, smsVerifyToken);
            case APPLE -> appleSocialLoginService.linkAccount(tempToken, smsVerifyToken);
            default -> throw new BusinessException(ErrorCode.SOCIAL_PROVIDER_TYPE_UNKNOWN,
                ErrorCode.SOCIAL_PROVIDER_TYPE_UNKNOWN.getDefaultMessage() + ": " + provider);
        };
    }

    @Override
    public SocialLoginResult naverLogin(String authorizationCode, String state) {
        return naverSocialLoginService.login(authorizationCode, state);
    }

    @Override
    public SocialLoginResult facebookLogin(String accessToken) {
        return facebookSocialLoginService.login(accessToken);
    }

    @Override
    public SocialLoginResult appleLogin(String authorizationCode) {
        return appleSocialLoginService.login(authorizationCode);
    }

    @Override
    public MemberJwtResult socialSignUp(AuthSocialSignUpCommand command) {
        MemberGender genderType = MemberGender.from(command.gender());
        return switch (MemberSocialProvider.from(command.provider())) {
            case KAKAO -> kakaoSocialLoginService.signUp(
                command.tempToken(), command.username(), command.nickname(), command.fullName(),
                genderType, command.birthDate(), command.phoneNumber(),
                command.pushNotificationEnabled(), command.marketingInfoEnabled(),
                command.eventInfoEnabled(), command.referrerNickname()
            );
            case NAVER -> naverSocialLoginService.signUp(
                command.tempToken(), command.username(), command.nickname(), command.fullName(),
                genderType, command.birthDate(), command.phoneNumber(),
                command.pushNotificationEnabled(), command.marketingInfoEnabled(),
                command.eventInfoEnabled(), command.referrerNickname()
            );
            case FACEBOOK -> facebookSocialLoginService.signUp(
                command.tempToken(), command.username(), command.nickname(), command.fullName(),
                genderType, command.birthDate(), command.phoneNumber(),
                command.pushNotificationEnabled(), command.marketingInfoEnabled(),
                command.eventInfoEnabled(), command.referrerNickname()
            );
            case APPLE -> appleSocialLoginService.signUp(
                command.tempToken(), command.username(), command.nickname(), command.fullName(),
                genderType, command.birthDate(), command.phoneNumber(),
                command.pushNotificationEnabled(), command.marketingInfoEnabled(),
                command.eventInfoEnabled(), command.referrerNickname()
            );
            default -> throw new BusinessException(ErrorCode.SOCIAL_PROVIDER_TYPE_UNKNOWN,
                ErrorCode.SOCIAL_PROVIDER_TYPE_UNKNOWN.getDefaultMessage() + ": " + command.provider());
        };
    }
}
