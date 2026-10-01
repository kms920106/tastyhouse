package com.tastyhouse.application.member.service;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.member.event.MemberWithdrawnEvent;
import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.domain.member.model.MemberWithdrawal;
import com.tastyhouse.domain.member.model.MemberWithdrawalReason;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.member.port.out.write.MemberPersistencePort;
import com.tastyhouse.application.member.port.out.write.MemberWithdrawalPersistencePort;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.marker.SharedApp;

@SharedApp
public class MemberWithdrawalService {

    private final MemberPersistencePort memberPersistencePort;
    private final MemberWithdrawalPersistencePort memberWithdrawalPersistencePort;
    private final DomainEventPublisher domainEventPublisher;

    public MemberWithdrawalService(
        MemberPersistencePort memberPersistencePort,
        MemberWithdrawalPersistencePort memberWithdrawalPersistencePort,
        DomainEventPublisher domainEventPublisher
    ) {
        this.memberPersistencePort = memberPersistencePort;
        this.memberWithdrawalPersistencePort = memberWithdrawalPersistencePort;
        this.domainEventPublisher = domainEventPublisher;
    }

    public void withdraw(MemberId memberId, MemberWithdrawalReason reason, String reasonDetail) {
        Member member = memberPersistencePort.findById(memberId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        member.withdraw();
        memberPersistencePort.save(member);

        memberWithdrawalPersistencePort.save(MemberWithdrawal.of(memberId, reason, reasonDetail));

        domainEventPublisher.publish(
            new MemberWithdrawnEvent(member.getMemberId(), reason, LocalDateTime.now())
        );
    }
}
