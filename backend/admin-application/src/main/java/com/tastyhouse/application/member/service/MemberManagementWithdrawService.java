package com.tastyhouse.application.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.model.MemberWithdrawalReason;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.member.port.in.MemberManagementWithdrawCommand;
import com.tastyhouse.application.member.port.in.MemberManagementWithdrawUseCase;

@Service
@Transactional
class MemberManagementWithdrawService implements MemberManagementWithdrawUseCase {

    private final MemberWithdrawalService memberWithdrawalService;

    public MemberManagementWithdrawService(MemberWithdrawalService memberWithdrawalService) {
        this.memberWithdrawalService = memberWithdrawalService;
    }

    @Override
    public void withdraw(MemberManagementWithdrawCommand command) {
        MemberId memberId = MemberId.of(command.memberId());
        memberWithdrawalService.withdraw(memberId, MemberWithdrawalReason.from(command.reason()), command.reasonDetail());
    }
}
