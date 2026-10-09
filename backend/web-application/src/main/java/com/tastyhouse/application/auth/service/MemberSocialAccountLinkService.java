package com.tastyhouse.application.auth.service;

import org.springframework.stereotype.Service;

import com.tastyhouse.application.auth.port.in.MemberSocialAccountLinkUseCase;
import com.tastyhouse.application.auth.port.out.SocialLinkResult;

@Service
class MemberSocialAccountLinkService implements MemberSocialAccountLinkUseCase {

    private final SocialLoginService socialLoginService;

    public MemberSocialAccountLinkService(SocialLoginService socialLoginService) {
        this.socialLoginService = socialLoginService;
    }

    @Override
    public SocialLinkResult linkAccount(String provider, String tempToken, String smsVerifyToken) {
        return socialLoginService.linkAccount(SocialLoginService.providerOf(provider), tempToken, smsVerifyToken);
    }
}
