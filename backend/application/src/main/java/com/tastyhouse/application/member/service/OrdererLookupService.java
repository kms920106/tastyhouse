package com.tastyhouse.application.member.service;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.domain.member.service.OrdererSnapshot;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.member.store.MemberRepository;

public class OrdererLookupService {
    private final MemberRepository memberRepository;

    public OrdererLookupService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public OrdererSnapshot findOrderer(MemberId memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        return new OrdererSnapshot(
            member.getFullName(),
            member.getPhoneNumber().value(),
            member.getUsername()
        );
    }
}
