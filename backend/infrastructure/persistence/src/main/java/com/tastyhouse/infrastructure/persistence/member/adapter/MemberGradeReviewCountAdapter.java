package com.tastyhouse.infrastructure.persistence.member.adapter;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.tastyhouse.application.member.port.out.MemberReviewCount;
import com.tastyhouse.application.member.port.out.MemberReviewCountPort;
import com.tastyhouse.infrastructure.persistence.review.query.MemberReviewCountQueryAdapter;
import com.tastyhouse.infrastructure.persistence.review.query.MemberReviewCountResult;

@Component
public class MemberGradeReviewCountAdapter implements MemberReviewCountPort {

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
