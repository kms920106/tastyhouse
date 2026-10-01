package com.tastyhouse.infrastructure.rank.persistence;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.tastyhouse.application.rank.port.out.MemberReviewCount;
import com.tastyhouse.application.rank.port.out.MemberReviewCountPort;
import com.tastyhouse.infrastructure.review.query.MemberReviewCountQueryAdapter;
import com.tastyhouse.infrastructure.review.query.MemberReviewCountResult;

@Component
public class MemberReviewCountAdapter implements MemberReviewCountPort {

    private final MemberReviewCountQueryAdapter memberReviewCountQueryAdapter;

    public MemberReviewCountAdapter(MemberReviewCountQueryAdapter memberReviewCountQueryAdapter) {
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
            result.reviewCount(),
            result.lastReviewAt()
        );
    }
}
