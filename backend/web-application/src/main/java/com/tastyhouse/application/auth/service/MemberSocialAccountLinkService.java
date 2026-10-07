package com.tastyhouse.application.auth.service;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;
import com.tastyhouse.domain.member.model.MemberSocialProvider;
import com.tastyhouse.application.auth.port.in.MemberSocialAccountLinkUseCase;
import com.tastyhouse.application.auth.port.out.SocialLinkResult;
import com.tastyhouse.application.auth.service.apple.AppleSocialLoginService;
import com.tastyhouse.application.auth.service.facebook.FacebookSocialLoginService;
import com.tastyhouse.application.auth.service.kakao.KakaoSocialLoginService;
import com.tastyhouse.application.auth.service.naver.NaverSocialLoginService;

@Service
class MemberSocialAccountLinkService implements MemberSocialAccountLinkUseCase {

    private final KakaoSocialLoginService kakaoSocialLoginService;
    private final NaverSocialLoginService naverSocialLoginService;
    private final FacebookSocialLoginService facebookSocialLoginService;
    private final AppleSocialLoginService appleSocialLoginService;

    public MemberSocialAccountLinkService(
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
    public SocialLinkResult linkAccount(String provider, String tempToken, String smsVerifyToken) {
        return switch (MemberSocialProvider.from(provider)) {
            case KAKAO -> kakaoSocialLoginService.linkAccount(tempToken, smsVerifyToken);
            case NAVER -> naverSocialLoginService.linkAccount(tempToken, smsVerifyToken);
            case FACEBOOK -> facebookSocialLoginService.linkAccount(tempToken, smsVerifyToken);
            case APPLE -> appleSocialLoginService.linkAccount(tempToken, smsVerifyToken);
            default -> throw new DomainException(DomainErrorCode.SOCIAL_PROVIDER_TYPE_UNKNOWN,
                DomainErrorCode.SOCIAL_PROVIDER_TYPE_UNKNOWN.getDefaultMessage() + ": " + provider);
        };
    }
}
