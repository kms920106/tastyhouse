package com.tastyhouse.application.member.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.tastyhouse.application.member.port.out.MemberReviewCount;
import com.tastyhouse.application.member.port.out.MemberReviewCountPort;
import com.tastyhouse.application.member.store.MemberRepository;
import com.tastyhouse.domain.member.model.MemberGrade;
import com.tastyhouse.domain.member.vo.MemberId;

public class GradeSettlementService {
    private static final LocalDateTime ALL_TIME_START = LocalDateTime.of(2000, 1, 1, 0, 0, 0);

    private final MemberReviewCountPort memberReviewCountPort;
    private final MemberRepository memberRepository;

    public GradeSettlementService(
        MemberReviewCountPort memberReviewCountPort,
        MemberRepository memberRepository
    ) {
        this.memberReviewCountPort = memberReviewCountPort;
        this.memberRepository = memberRepository;
    }

    public long settleAll(LocalDateTime now) {
        List<MemberReviewCount> reviewCounts =
            memberReviewCountPort.countReviewsByMemberWithPeriod(ALL_TIME_START, now);

        long totalUpdated = 0;
        for (Map.Entry<MemberGrade, List<Long>> entry : groupMembersByGrade(reviewCounts).entrySet()) {
            List<Long> memberIds = entry.getValue();
            if (!memberIds.isEmpty()) {
                totalUpdated += memberRepository.bulkUpdateGrade(memberIds, entry.getKey());
            }
        }

        return totalUpdated;
    }

    private Map<MemberGrade, List<Long>> groupMembersByGrade(List<MemberReviewCount> reviewCounts) {
        Map<MemberGrade, List<Long>> gradeGroups = new EnumMap<>(MemberGrade.class);
        for (MemberGrade grade : MemberGrade.values()) {
            gradeGroups.put(grade, new ArrayList<>());
        }

        for (MemberReviewCount reviewCount : reviewCounts) {
            MemberGrade grade = MemberGrade.fromReviewCount(reviewCount.reviewCount().intValue());
            gradeGroups.get(grade).add(MemberId.of(reviewCount.memberId()).value());
        }

        return gradeGroups;
    }
}
