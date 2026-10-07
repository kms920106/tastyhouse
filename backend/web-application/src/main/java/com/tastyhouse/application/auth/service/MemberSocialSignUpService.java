package com.tastyhouse.application.auth.service;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;
import com.tastyhouse.domain.member.model.MemberGender;
import com.tastyhouse.domain.member.model.MemberSocialProvider;
import com.tastyhouse.application.auth.port.in.AuthSocialSignUpCommand;
import com.tastyhouse.application.auth.port.in.MemberSocialSignUpUseCase;
import com.tastyhouse.application.auth.port.out.MemberJwtResult;
import com.tastyhouse.application.auth.service.apple.AppleSocialLoginService;
import com.tastyhouse.application.auth.service.facebook.FacebookSocialLoginService;
import com.tastyhouse.application.auth.service.kakao.KakaoSocialLoginService;
import com.tastyhouse.application.auth.service.naver.NaverSocialLoginService;

@Service
class MemberSocialSignUpService implements MemberSocialSignUpUseCase {

    private final KakaoSocialLoginService kakaoSocialLoginService;
    private final NaverSocialLoginService naverSocialLoginService;
    private final FacebookSocialLoginService facebookSocialLoginService;
    private final AppleSocialLoginService appleSocialLoginService;

    public MemberSocialSignUpService(
        KakaoSocialLoginService kakaoSocialLoginService,
        NaverSocialLoginService naverSocialLoginService,
        FacebookSocialLoginService facebookSocialLoginService,
        AppleSocialLoginService appleSocialLoginService
    ) {
        this.kakaoSocialLoginService = kakaoSocialLoginService;
        this.naverSocialLoginService = naverSocialLoginService;
        this.facebookSocialLoginService = facebookSocialLoginService;
        this.appleSocialLoginService = appleSocialLoginService;
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
            default -> throw new DomainException(DomainErrorCode.SOCIAL_PROVIDER_TYPE_UNKNOWN,
                DomainErrorCode.SOCIAL_PROVIDER_TYPE_UNKNOWN.getDefaultMessage() + ": " + command.provider());
        };
    }
}
