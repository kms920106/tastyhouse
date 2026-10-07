package com.tastyhouse.application.member.service;

import org.springframework.stereotype.Service;

import com.tastyhouse.application.member.port.in.MemberPasswordVerifyUseCase;

@Service
class MemberPasswordVerifyService implements MemberPasswordVerifyUseCase {

    private final MemberAuthService memberAuthService;

    public MemberPasswordVerifyService(MemberAuthService memberAuthService) {
        this.memberAuthService = memberAuthService;
    }

    @Override
    public String verifyPasswordAndIssueToken(Long memberId, String password) {
        memberAuthService.verifyPassword(memberId, password);
        return memberAuthService.createPersonalInfoVerifyToken(memberId);
    }
}
