package com.tastyhouse.application.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.model.MemberWithdrawalReason;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.member.port.in.MemberWithdrawCommand;
import com.tastyhouse.application.member.port.in.MemberWithdrawUseCase;

@Service
@Transactional
class MemberWithdrawService implements MemberWithdrawUseCase {

    private final MemberWithdrawalService memberWithdrawalService;

    public MemberWithdrawService(MemberWithdrawalService memberWithdrawalService) {
        this.memberWithdrawalService = memberWithdrawalService;
    }

    @Override
    public void withdraw(MemberWithdrawCommand command) {
        memberWithdrawalService.withdraw(
            MemberId.of(command.memberId()),
            MemberWithdrawalReason.from(command.reason()),
            command.reasonDetail()
        );
    }
}
