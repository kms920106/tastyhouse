package com.tastyhouse.application.member.service;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.domain.member.model.OrdererSnapshot;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.member.port.out.write.MemberLoadPort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
public class OrdererLookupService {

    private final MemberLoadPort memberLoadPort;

    public OrdererLookupService(MemberLoadPort memberLoadPort) {
        this.memberLoadPort = memberLoadPort;
    }

    public OrdererSnapshot findOrderer(MemberId memberId) {
        Member member = memberLoadPort.findById(memberId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.MEMBER_NOT_FOUND));

        return new OrdererSnapshot(
            member.getFullName(),
            member.getPhoneNumber().value(),
            member.getUsername()
        );
    }
}
