package com.tastyhouse.application.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.domain.member.model.MemberWithdrawalReason;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.member.port.in.MemberActivateCommand;
import com.tastyhouse.application.member.port.in.MemberManagementCommandUseCase;
import com.tastyhouse.application.member.port.in.MemberManagementWithdrawCommand;
import com.tastyhouse.application.member.port.in.MemberSuspendCommand;
import com.tastyhouse.application.member.port.out.write.MemberPersistencePort;
import com.tastyhouse.application.shared.marker.AdminApp;

@Service
@AdminApp
@Transactional
public class MemberManagementCommandService implements MemberManagementCommandUseCase {

    private final MemberPersistencePort memberPersistencePort;
    private final MemberWithdrawalService memberWithdrawalService;

    public MemberManagementCommandService(MemberPersistencePort memberPersistencePort, MemberWithdrawalService memberWithdrawalService) {
        this.memberPersistencePort = memberPersistencePort;
        this.memberWithdrawalService = memberWithdrawalService;
    }

    @Override
    public void suspend(MemberSuspendCommand command) {
        MemberId memberId = MemberId.of(command.memberId());
        Member member = loadMember(memberId);
        member.suspend();
        memberPersistencePort.save(member);
    }

    @Override
    public void activate(MemberActivateCommand command) {
        MemberId memberId = MemberId.of(command.memberId());
        Member member = loadMember(memberId);
        member.activate();
        memberPersistencePort.save(member);
    }

    @Override
    public void withdraw(MemberManagementWithdrawCommand command) {
        MemberId memberId = MemberId.of(command.memberId());
        memberWithdrawalService.withdraw(memberId, MemberWithdrawalReason.from(command.reason()), command.reasonDetail());
    }

    private Member loadMember(MemberId memberId) {
        return memberPersistencePort.findById(memberId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
