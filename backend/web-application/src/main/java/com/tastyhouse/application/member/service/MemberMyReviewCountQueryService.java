package com.tastyhouse.application.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.member.port.in.MemberMyReviewCountQueryUseCase;

@Service
@Transactional(readOnly = true)
class MemberMyReviewCountQueryService implements MemberMyReviewCountQueryUseCase {

    private final MemberReviewService memberReviewService;

    public MemberMyReviewCountQueryService(MemberReviewService memberReviewService) {
        this.memberReviewService = memberReviewService;
    }

    @Override
    public long getMyReviewCount(Long memberId) {
        return memberReviewService.getMyReviewCount(memberId);
    }
}
