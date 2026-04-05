package com.tastyhouse.core.service;

import com.tastyhouse.core.entity.rank.MemberReviewRank;
import com.tastyhouse.core.entity.rank.RankType;
import com.tastyhouse.core.entity.rank.dto.MemberReviewCountDto;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankAggregationService {

    private final RankCoreService rankCoreService;
    private final ReviewCoreService reviewCoreService;

    @Transactional
    public void aggregateAllRanks() {
        log.info("=== 전체 랭킹 집계 시작 ===");
        LocalDate today = LocalDate.now();

        aggregateRankByType(RankType.ALL, today);
        aggregateRankByType(RankType.MONTHLY, today);
        aggregateRankByType(RankType.WEEKLY, today);

        log.info("=== 전체 랭킹 집계 완료 ===");
    }

    @Transactional
    public void aggregateRankByType(RankType rankType, LocalDate baseDate) {
        aggregateRankByType(rankType, baseDate, 10);
    }

    @Transactional
    public void aggregateRankByType(RankType rankType, LocalDate baseDate, int limit) {
        log.info("랭킹 집계 시작: type={}, baseDate={}, limit={}", rankType, baseDate, limit);

        LocalDateTime[] period = calculatePeriod(rankType, baseDate);
        LocalDateTime startDate = period[0];
        LocalDateTime endDate = period[1];

        log.info("집계 기간: {} ~ {}", startDate, endDate);

        List<MemberReviewCountDto> reviewCounts = reviewCoreService.countReviewsByMemberWithPeriod(startDate, endDate);

        log.info("집계된 유저 수: {}", reviewCounts.size());

        // 상위 N개만 저장
        List<MemberReviewCountDto> topReviewCounts = reviewCounts.stream()
            .limit(limit)
            .toList();

        List<MemberReviewRank> ranks = buildRanks(topReviewCounts, rankType, baseDate);

        rankCoreService.deleteOldRanks(rankType, baseDate);
        rankCoreService.saveAllRanks(ranks);

        log.info("랭킹 저장 완료: {} 건 (전체 {} 명 중 상위 {}명)", ranks.size(), reviewCounts.size(), limit);
    }

    private List<MemberReviewRank> buildRanks(
        List<MemberReviewCountDto> reviewCounts,
        RankType rankType,
        LocalDate baseDate
    ) {
        List<MemberReviewRank> ranks = new ArrayList<>();
        for (int i = 0; i < reviewCounts.size(); i++) {
            MemberReviewCountDto dto = reviewCounts.get(i);
            int currentRank = i + 1;

            MemberReviewRank rank = MemberReviewRank.builder()
                .memberId(dto.memberId())
                .reviewCount(dto.reviewCount().intValue())
                .rankNo(currentRank)
                .rankType(rankType)
                .baseDate(baseDate)
                .lastReviewAt(dto.lastReviewAt())
                .build();

            ranks.add(rank);
        }

        return ranks;
    }

    private LocalDateTime[] calculatePeriod(RankType rankType, LocalDate baseDate) {
        LocalDateTime startDate;
        LocalDateTime endDate;

        switch (rankType) {
            case ALL:
                startDate = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
                endDate = LocalDateTime.of(baseDate, LocalTime.MAX);
                break;

            case MONTHLY:
                YearMonth yearMonth = YearMonth.from(baseDate);
                startDate = LocalDateTime.of(yearMonth.atDay(1), LocalTime.MIN);
                endDate = LocalDateTime.of(yearMonth.atEndOfMonth(), LocalTime.MAX);
                break;

            case WEEKLY:
                LocalDate weekStart = baseDate.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
                LocalDate weekEnd = weekStart.plusDays(6);
                startDate = LocalDateTime.of(weekStart, LocalTime.MIN);
                endDate = LocalDateTime.of(weekEnd, LocalTime.MAX);
                break;

            default:
                throw new BusinessException(ErrorCode.RANK_TYPE_UNKNOWN,
                    ErrorCode.RANK_TYPE_UNKNOWN.getDefaultMessage() + ": " + rankType);
        }

        return new LocalDateTime[]{startDate, endDate};
    }
}
