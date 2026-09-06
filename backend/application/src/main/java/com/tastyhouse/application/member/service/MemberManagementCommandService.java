package com.tastyhouse.application.member.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.member.port.in.MemberActivateCommand;
import com.tastyhouse.application.member.port.in.MemberManagementCommandUseCase;
import com.tastyhouse.application.member.port.in.MemberSuspendCommand;
import com.tastyhouse.application.member.port.in.MemberManagementWithdrawCommand;
import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.domain.member.model.MemberWithdrawalReason;
import com.tastyhouse.domain.member.repository.MemberRepository;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.member.service.MemberWithdrawalService;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

@Service
@AdminApp
@Transactional
public class MemberManagementCommandService implements MemberManagementCommandUseCase {

    private final MemberRepository memberRepository;
    private final MemberWithdrawalService memberWithdrawalService;

    public MemberManagementCommandService(MemberRepository memberRepository, MemberWithdrawalService memberWithdrawalService) {
        this.memberRepository = memberRepository;
        this.memberWithdrawalService = memberWithdrawalService;
    }

    @Override
    public void suspend(MemberSuspendCommand command) {
        MemberId memberId = MemberId.of(command.memberId());
        Member member = loadMember(memberId);
        member.suspend();
        memberRepository.save(member);
    }

    @Override
    public void activate(MemberActivateCommand command) {
        MemberId memberId = MemberId.of(command.memberId());
        Member member = loadMember(memberId);
        member.activate();
        memberRepository.save(member);
    }

    @Override
    public void withdraw(MemberManagementWithdrawCommand command) {
        MemberId memberId = MemberId.of(command.memberId());
        memberWithdrawalService.withdraw(memberId, MemberWithdrawalReason.from(command.reason()), command.reasonDetail());
    }

    private Member loadMember(MemberId memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
