package com.tastyhouse.infrastructure.member.adapter;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.tastyhouse.domain.member.port.MemberReviewCount;
import com.tastyhouse.domain.member.port.MemberReviewCountPort;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.infrastructure.review.query.MemberReviewCountQueryDao;
import com.tastyhouse.infrastructure.review.query.MemberReviewCountResult;

@Component
public class MemberGradeReviewCountAdapter implements MemberReviewCountPort {
    private final MemberReviewCountQueryDao memberReviewCountQueryDao;

    public MemberGradeReviewCountAdapter(MemberReviewCountQueryDao memberReviewCountQueryDao) {
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
            result.reviewCount()
        );
    }
}
