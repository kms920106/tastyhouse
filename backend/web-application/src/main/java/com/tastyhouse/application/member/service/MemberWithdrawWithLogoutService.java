package com.tastyhouse.application.member.service;

import org.springframework.stereotype.Service;

import com.tastyhouse.application.member.port.in.MemberWithdrawCommand;
import com.tastyhouse.application.member.port.in.MemberWithdrawUseCase;
import com.tastyhouse.application.member.port.in.MemberWithdrawWithLogoutUseCase;

@Service
class MemberWithdrawWithLogoutService implements MemberWithdrawWithLogoutUseCase {

    private final MemberWithdrawUseCase memberWithdrawUseCase;
    private final MemberAuthService memberAuthService;

    public MemberWithdrawWithLogoutService(
        MemberWithdrawUseCase memberWithdrawUseCase,
        MemberAuthService memberAuthService
    ) {
        this.memberWithdrawUseCase = memberWithdrawUseCase;
        this.memberAuthService = memberAuthService;
    }

    @Override
    public void withdrawMember(MemberWithdrawCommand command, String bearerToken) {
        memberWithdrawUseCase.withdraw(command);
        memberAuthService.invalidateAccessToken(bearerToken);
    }
}
