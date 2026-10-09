package com.tastyhouse.application.auth.service;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.member.model.MemberGender;
import com.tastyhouse.application.auth.port.in.AuthSocialSignUpCommand;
import com.tastyhouse.application.auth.port.in.MemberSocialSignUpUseCase;
import com.tastyhouse.application.auth.port.out.MemberJwtResult;
import com.tastyhouse.application.auth.port.out.SocialProvider;

@Service
class MemberSocialSignUpService implements MemberSocialSignUpUseCase {

    private final SocialLoginService socialLoginService;

    public MemberSocialSignUpService(SocialLoginService socialLoginService) {
        this.socialLoginService = socialLoginService;
    }

    @Override
    public MemberJwtResult socialSignUp(AuthSocialSignUpCommand command) {
        MemberGender genderType = MemberGender.from(command.gender());
        SocialProvider provider = SocialLoginService.providerOf(command.provider());
        return socialLoginService.signUp(
            provider, command.tempToken(), command.username(), command.nickname(), command.fullName(),
            genderType, command.birthDate(), command.phoneNumber(),
            command.pushNotificationEnabled(), command.marketingInfoEnabled(),
            command.eventInfoEnabled(), command.referrerNickname()
        );
    }
}
