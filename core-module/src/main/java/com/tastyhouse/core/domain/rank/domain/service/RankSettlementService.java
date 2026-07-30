package com.tastyhouse.core.domain.rank.domain.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import com.tastyhouse.core.domain.rank.domain.model.MemberReviewRank;
import com.tastyhouse.core.domain.rank.domain.model.RankType;
import com.tastyhouse.core.domain.rank.domain.port.MemberReviewCount;
import com.tastyhouse.core.domain.rank.domain.port.MemberReviewCountPort;
import com.tastyhouse.core.domain.rank.domain.repository.MemberReviewRankRepository;

/**
 * 랭킹 확정(도메인 서비스).
 *
 * <p>한 랭킹 타입의 집계는 "기간 산정 → 기간 내 회원별 리뷰 수 조회 → 상위 N명 순위 부여 → 같은
 * 기준일의 기존 랭킹 일괄 삭제 → 신규 랭킹 일괄 적재"가 반드시 함께 일어나야 하는 원자 연산이다. 기존
 * 행을 지우고 새로 넣는 사이에 실패하면 그 기준일의 랭킹이 사라지므로, "삭제와 적재는 항상 함께
 * 움직인다"는 불변식을 도메인 계층에 둔다(분류 C).
 *
 * <p>이 연산의 트리거는 액터와 무관하다 — batch 스케줄러의 야간 자동 집계와 관리자의 수동 재집계가
 * 같은 규칙을 써야 하므로, 어느 한 소비 모듈에 두지 않고 도메인 서비스로 하강시켜 양쪽이 공유한다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code DomainServiceConfig}가 담당한다. 트랜잭션 경계는 이 서비스를 호출하는
 * 소비 모듈의 command 서비스가 선언한다.
 */
public class RankSettlementService {

    /**
     * 전체 기간({@link RankType#ALL}) 집계의 시작 시각 — 서비스 개시 이전으로 충분히 이른 고정값.
     */
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

    /**
     * 전체·월간·주간 랭킹을 기준일로 모두 재집계한다.
     *
     * @return 타입별 적재 건수 합계
     */
    public int settleAll(LocalDate baseDate) {
        int settled = 0;
        settled += settle(RankType.ALL, baseDate, DEFAULT_LIMIT);
        settled += settle(RankType.MONTHLY, baseDate, DEFAULT_LIMIT);
        settled += settle(RankType.WEEKLY, baseDate, DEFAULT_LIMIT);
        return settled;
    }

    /**
     * 한 랭킹 타입을 기준일로 재집계한다 — 기존 랭킹을 지우고 상위 {@code limit}명을 새로 적재한다.
     *
     * @return 적재된 랭킹 건수
     */
    public int settle(RankType rankType, LocalDate baseDate, int limit) {
        LocalDateTime startAt = periodStartAt(rankType, baseDate);
        LocalDateTime endAt = periodEndAt(rankType, baseDate);

        List<MemberReviewCount> reviewCounts = memberReviewCountPort.countReviewsByMemberWithPeriod(startAt, endAt);
        List<MemberReviewRank> ranks = buildRanks(reviewCounts.stream().limit(limit).toList(), rankType, baseDate);

        memberReviewRankRepository.deleteByRankTypeAndBaseDate(rankType, baseDate);
        memberReviewRankRepository.saveAll(ranks);

        return ranks.size();
    }

    /**
     * 조회 순서(리뷰 수 내림차순)를 그대로 1위부터의 순위로 부여한다.
     */
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
