package com.tastyhouse.infrastructure.jpa.member.adapter;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.tastyhouse.application.member.port.out.MemberReviewCount;
import com.tastyhouse.application.member.port.out.MemberReviewCountPort;
import com.tastyhouse.infrastructure.jpa.review.query.MemberReviewCountQueryAdapter;
import com.tastyhouse.infrastructure.jpa.review.query.MemberReviewCountResult;

@Component
class MemberGradeReviewCountAdapter implements MemberReviewCountPort {

    private final MemberReviewCountQueryAdapter memberReviewCountQueryAdapter;

    public MemberGradeReviewCountAdapter(MemberReviewCountQueryAdapter memberReviewCountQueryAdapter) {
        this.memberReviewCountQueryAdapter = memberReviewCountQueryAdapter;
    }

    @Override
    public List<MemberReviewCount> countReviewsByMemberWithPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return memberReviewCountQueryAdapter.countReviewsByMemberWithPeriod(startDate, endDate).stream()
            .map(this::toMemberReviewCount)
            .toList();
    }

    private MemberReviewCount toMemberReviewCount(MemberReviewCountResult result) {
        return MemberReviewCount.of(
            result.memberId(),
            result.reviewCount()
        );
    }
}
