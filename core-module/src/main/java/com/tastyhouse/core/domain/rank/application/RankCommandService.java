package com.tastyhouse.core.domain.rank.application;

import com.tastyhouse.core.domain.rank.application.dto.result.MemberReviewCountResult;
import com.tastyhouse.core.domain.rank.domain.model.MemberReviewRank;
import com.tastyhouse.core.domain.rank.domain.model.RankType;
import com.tastyhouse.core.domain.rank.domain.repository.MemberReviewRankRepository;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.service.ReviewCoreService;
import jakarta.persistence.EntityManager;
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
@Transactional
@RequiredArgsConstructor
public class RankCommandService {

    private final MemberReviewRankRepository memberReviewRankRepository;
    private final ReviewCoreService reviewCoreService;
    private final EntityManager entityManager;

    public void saveAllRanks(List<MemberReviewRank> ranks) {
        memberReviewRankRepository.saveAll(ranks);
    }

    public void deleteOldRanks(RankType rankType, LocalDate baseDate) {
        memberReviewRankRepository.deleteByRankTypeAndBaseDate(rankType, baseDate);
        entityManager.flush();
        entityManager.clear();
    }

    public void aggregateAllRanks() {
        log.info("=== 전체 랭킹 집계 시작 ===");
        LocalDate today = LocalDate.now();

        aggregateRankByType(RankType.ALL, today);
        aggregateRankByType(RankType.MONTHLY, today);
        aggregateRankByType(RankType.WEEKLY, today);

        log.info("=== 전체 랭킹 집계 완료 ===");
    }

    public void aggregateRankByType(RankType rankType, LocalDate baseDate) {
        aggregateRankByType(rankType, baseDate, 10);
    }

    public void aggregateRankByType(RankType rankType, LocalDate baseDate, int limit) {
        log.info("랭킹 집계 시작: type={}, baseDate={}, limit={}", rankType, baseDate, limit);

        LocalDateTime[] period = calculatePeriod(rankType, baseDate);
        LocalDateTime startDate = period[0];
        LocalDateTime endDate = period[1];

        log.info("집계 기간: {} ~ {}", startDate, endDate);

        List<MemberReviewCountResult> reviewCounts = reviewCoreService.countReviewsByMemberWithPeriod(startDate, endDate);

        log.info("집계된 유저 수: {}", reviewCounts.size());

        List<MemberReviewCountResult> topReviewCounts = reviewCounts.stream()
            .limit(limit)
            .toList();

        List<MemberReviewRank> ranks = buildRanks(topReviewCounts, rankType, baseDate);

        deleteOldRanks(rankType, baseDate);
        saveAllRanks(ranks);

        log.info("랭킹 저장 완료: {} 건 (전체 {} 명 중 상위 {}명)", ranks.size(), reviewCounts.size(), limit);
    }

    private List<MemberReviewRank> buildRanks(
        List<MemberReviewCountResult> reviewCounts,
        RankType rankType,
        LocalDate baseDate
    ) {
        List<MemberReviewRank> ranks = new ArrayList<>();
        for (int i = 0; i < reviewCounts.size(); i++) {
            MemberReviewCountResult dto = reviewCounts.get(i);
            ranks.add(MemberReviewRank.of(
                dto.memberId(),
                dto.reviewCount().intValue(),
                i + 1,
                rankType,
                baseDate,
                dto.lastReviewAt()
            ));
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
