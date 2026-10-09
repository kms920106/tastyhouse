package com.tastyhouse.application.member.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.member.event.MemberWithdrawnEvent;
import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.domain.member.model.MemberWithdrawal;
import com.tastyhouse.domain.member.model.MemberWithdrawalReason;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.member.port.out.write.MemberLoadPort;
import com.tastyhouse.application.member.port.out.write.MemberSavePort;
import com.tastyhouse.application.member.port.out.write.MemberWithdrawalSavePort;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
public class MemberWithdrawalService {

    private final MemberLoadPort memberLoadPort;
    private final MemberSavePort memberSavePort;
    private final MemberWithdrawalSavePort memberWithdrawalSavePort;
    private final DomainEventPublisher domainEventPublisher;

    public MemberWithdrawalService(
        MemberLoadPort memberLoadPort,
        MemberSavePort memberSavePort,
        MemberWithdrawalSavePort memberWithdrawalSavePort,
        DomainEventPublisher domainEventPublisher
    ) {
        this.memberLoadPort = memberLoadPort;
        this.memberSavePort = memberSavePort;
        this.memberWithdrawalSavePort = memberWithdrawalSavePort;
        this.domainEventPublisher = domainEventPublisher;
    }

    public void withdraw(MemberId memberId, MemberWithdrawalReason reason, String reasonDetail) {
        Member member = memberLoadPort.findById(memberId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.MEMBER_NOT_FOUND));

        member.withdraw();
        memberSavePort.save(member);

        memberWithdrawalSavePort.save(MemberWithdrawal.of(memberId, reason, reasonDetail));

        domainEventPublisher.publish(
            new MemberWithdrawnEvent(member.getMemberId(), reason, LocalDateTime.now())
        );
    }
}
