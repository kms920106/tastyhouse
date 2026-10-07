package com.tastyhouse.application.member.service;

import org.springframework.stereotype.Service;

import com.tastyhouse.application.member.port.in.MemberPersonalInfoUpdateCommand;
import com.tastyhouse.application.member.port.in.MemberPersonalInfoUpdateUseCase;
import com.tastyhouse.application.member.port.in.MemberVerifiedPersonalInfoUpdateUseCase;

@Service
class MemberVerifiedPersonalInfoUpdateService implements MemberVerifiedPersonalInfoUpdateUseCase {

    private final MemberAuthService memberAuthService;
    private final MemberPersonalInfoUpdateUseCase memberPersonalInfoUpdateUseCase;

    public MemberVerifiedPersonalInfoUpdateService(
        MemberAuthService memberAuthService,
        MemberPersonalInfoUpdateUseCase memberPersonalInfoUpdateUseCase
    ) {
        this.memberAuthService = memberAuthService;
        this.memberPersonalInfoUpdateUseCase = memberPersonalInfoUpdateUseCase;
    }

    @Override
    public void updatePersonalInfo(MemberPersonalInfoUpdateCommand command, String verifyToken, String smsVerifyToken) {
        Long memberId = command.memberId();
        String phoneNumber = command.phoneNumber();
        memberAuthService.verifyPersonalInfoToken(memberId, verifyToken);
        if (phoneNumber != null) {
            memberAuthService.verifyPhoneToken(memberId, smsVerifyToken, phoneNumber);
        }
        memberPersonalInfoUpdateUseCase.updatePersonalInfo(command);
    }
}
