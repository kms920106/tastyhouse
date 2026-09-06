package com.tastyhouse.domain.rank.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import com.tastyhouse.domain.rank.model.MemberReviewRank;
import com.tastyhouse.domain.rank.model.RankType;
import com.tastyhouse.domain.rank.port.MemberReviewCount;
import com.tastyhouse.domain.rank.port.MemberReviewCountPort;
import com.tastyhouse.domain.rank.repository.MemberReviewRankRepository;

public class RankSettlementService {
    private static final LocalDateTime ALL_TIME_START = LocalDateTime.of(2000, 1, 1, 0, 0, 0);

    private static final int DEFAULT_LIMIT = 10;

    private final MemberReviewRankRepository memberReviewRankRepository;
    private final MemberReviewCountPort memberReviewCountPort;

    public RankSettlementService(
        MemberReviewRankRepository memberReviewRankRepository,
        MemberReviewCountPort memberReviewCountPort
    ) {
        this.memberReviewRankRepository = memberReviewRankRepository;
        this.memberReviewCountPort = memberReviewCountPort;
    }

    public int settleAll(LocalDate baseDate) {
        int settled = 0;
        settled += settle(RankType.ALL, baseDate, DEFAULT_LIMIT);
        settled += settle(RankType.MONTHLY, baseDate, DEFAULT_LIMIT);
        settled += settle(RankType.WEEKLY, baseDate, DEFAULT_LIMIT);
        return settled;
    }

    public int settle(RankType rankType, LocalDate baseDate, int limit) {
        LocalDateTime startAt = periodStartAt(rankType, baseDate);
        LocalDateTime endAt = periodEndAt(rankType, baseDate);

        List<MemberReviewCount> reviewCounts = memberReviewCountPort.countReviewsByMemberWithPeriod(startAt, endAt);
        List<MemberReviewRank> ranks = buildRanks(reviewCounts.stream().limit(limit).toList(), rankType, baseDate);

        memberReviewRankRepository.deleteByRankTypeAndBaseDate(rankType, baseDate);
        memberReviewRankRepository.saveAll(ranks);

        return ranks.size();
    }

    private List<MemberReviewRank> buildRanks(
        List<MemberReviewCount> reviewCounts,
        RankType rankType,
        LocalDate baseDate
    ) {
        List<MemberReviewRank> ranks = new ArrayList<>();
        for (int i = 0; i < reviewCounts.size(); i++) {
            MemberReviewCount reviewCount = reviewCounts.get(i);
            ranks.add(MemberReviewRank.of(
                reviewCount.memberId(),
                reviewCount.reviewCount().intValue(),
                i + 1,
                rankType,
                baseDate,
                reviewCount.lastReviewAt()
            ));
        }
        return ranks;
    }

    private LocalDateTime periodStartAt(RankType rankType, LocalDate baseDate) {
        return switch (rankType) {
            case ALL -> ALL_TIME_START;
            case MONTHLY -> LocalDateTime.of(YearMonth.from(baseDate).atDay(1), LocalTime.MIN);
            case WEEKLY -> LocalDateTime.of(weekStart(baseDate), LocalTime.MIN);
        };
    }

    private LocalDateTime periodEndAt(RankType rankType, LocalDate baseDate) {
        return switch (rankType) {
            case ALL -> LocalDateTime.of(baseDate, LocalTime.MAX);
            case MONTHLY -> LocalDateTime.of(YearMonth.from(baseDate).atEndOfMonth(), LocalTime.MAX);
            case WEEKLY -> LocalDateTime.of(weekStart(baseDate).plusDays(6), LocalTime.MAX);
        };
    }

    private LocalDate weekStart(LocalDate baseDate) {
        return baseDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }
}
