package com.tastyhouse.application.member.service;

import org.springframework.stereotype.Service;

import com.tastyhouse.application.member.port.in.MemberPasswordUpdateCommand;
import com.tastyhouse.application.member.port.in.MemberPasswordUpdateUseCase;
import com.tastyhouse.application.member.port.in.MemberVerifiedPasswordUpdateUseCase;

@Service
class MemberVerifiedPasswordUpdateService implements MemberVerifiedPasswordUpdateUseCase {

    private final MemberAuthService memberAuthService;
    private final MemberPasswordUpdateUseCase memberPasswordUpdateUseCase;

    public MemberVerifiedPasswordUpdateService(
        MemberAuthService memberAuthService,
        MemberPasswordUpdateUseCase memberPasswordUpdateUseCase
    ) {
        this.memberAuthService = memberAuthService;
        this.memberPasswordUpdateUseCase = memberPasswordUpdateUseCase;
    }

    @Override
    public void updatePassword(MemberPasswordUpdateCommand command, String verifyToken) {
        memberAuthService.verifyPersonalInfoToken(command.memberId(), verifyToken);
        memberPasswordUpdateUseCase.updatePassword(command);
    }
}
