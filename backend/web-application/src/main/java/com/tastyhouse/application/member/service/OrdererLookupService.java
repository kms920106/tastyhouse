package com.tastyhouse.application.member.service;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.domain.member.model.OrdererSnapshot;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.member.port.out.write.MemberPersistencePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
public class OrdererLookupService {

    private final MemberPersistencePort memberPersistencePort;

    public OrdererLookupService(MemberPersistencePort memberPersistencePort) {
        this.memberPersistencePort = memberPersistencePort;
    }

    public OrdererSnapshot findOrderer(MemberId memberId) {
        Member member = memberPersistencePort.findById(memberId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.MEMBER_NOT_FOUND));

        return new OrdererSnapshot(
            member.getFullName(),
            member.getPhoneNumber().value(),
            member.getUsername()
        );
    }
}
