package com.tastyhouse.application.auth.service;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.member.model.MemberGender;
import com.tastyhouse.application.auth.port.in.AuthSignUpCommand;
import com.tastyhouse.application.auth.port.in.MemberSignUpUseCase;

@Service
class MemberSignUpService implements MemberSignUpUseCase {

    private final CredentialLoginService credentialLoginService;

    public MemberSignUpService(CredentialLoginService credentialLoginService) {
        this.credentialLoginService = credentialLoginService;
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
}
