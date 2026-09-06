package com.tastyhouse.infrastructure.rank.persistence;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.rank.port.MemberReviewCount;
import com.tastyhouse.domain.rank.port.MemberReviewCountPort;
import com.tastyhouse.infrastructure.review.query.MemberReviewCountQueryDao;
import com.tastyhouse.infrastructure.review.query.MemberReviewCountResult;

@Component
public class MemberReviewCountAdapter implements MemberReviewCountPort {
    private final MemberReviewCountQueryDao memberReviewCountQueryDao;

    public MemberReviewCountAdapter(MemberReviewCountQueryDao memberReviewCountQueryDao) {
        this.memberReviewCountQueryDao = memberReviewCountQueryDao;
    }

    @Override
    public List<MemberReviewCount> countReviewsByMemberWithPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return memberReviewCountQueryDao.countReviewsByMemberWithPeriod(startDate, endDate).stream()
            .map(this::toMemberReviewCount)
            .toList();
    }

    private MemberReviewCount toMemberReviewCount(MemberReviewCountResult result) {
        return MemberReviewCount.of(
            MemberId.of(result.memberId()),
            result.reviewCount(),
            result.lastReviewAt()
        );
    }
}
