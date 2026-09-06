package com.tastyhouse.domain.member.service;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.event.MemberWithdrawnEvent;
import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.domain.member.model.MemberWithdrawal;
import com.tastyhouse.domain.member.model.MemberWithdrawalReason;
import com.tastyhouse.domain.member.repository.MemberRepository;
import com.tastyhouse.domain.member.repository.MemberWithdrawalRepository;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;

public class MemberWithdrawalService {
    private final MemberRepository memberRepository;
    private final MemberWithdrawalRepository memberWithdrawalRepository;
    private final DomainEventPublisher domainEventPublisher;

    public MemberWithdrawalService(
        MemberRepository memberRepository,
        MemberWithdrawalRepository memberWithdrawalRepository,
        DomainEventPublisher domainEventPublisher
    ) {
        this.memberRepository = memberRepository;
        this.memberWithdrawalRepository = memberWithdrawalRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    public void withdraw(MemberId memberId, MemberWithdrawalReason reason, String reasonDetail) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        member.withdraw();
        memberRepository.save(member);

        memberWithdrawalRepository.save(MemberWithdrawal.of(memberId, reason, reasonDetail));

        domainEventPublisher.publish(
            new MemberWithdrawnEvent(member.getMemberId(), reason, LocalDateTime.now())
        );
    }
}
