package com.tastyhouse.core.domain.rank.domain.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.rank.domain.model.MemberReviewRank;
import com.tastyhouse.core.domain.rank.domain.model.RankType;
import com.tastyhouse.core.domain.rank.domain.port.MemberReviewCount;
import com.tastyhouse.core.domain.rank.domain.port.MemberReviewCountPort;
import com.tastyhouse.core.domain.rank.domain.repository.MemberReviewRankRepository;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 랭킹 확정 도메인 서비스 단위 테스트.
 *
 * <p>순수 POJO(도메인 서비스)이므로 Spring 컨텍스트·JPA 없이 write 포트와 조회 포트를 손으로 만든
 * 스텁으로 대체해 검증한다(도메인 서비스 하강으로 얻는 테스트 용이성의 레퍼런스).
 */
class RankSettlementServiceTest {

    private static final LocalDate BASE_DATE = LocalDate.of(2026, 7, 30); // 목요일

    @Test
    @DisplayName("조회 순서대로 1위부터 순위를 부여하고, 기존 랭킹 삭제 후 신규 랭킹을 적재한다")
    void settle_assignsRanksInOrderAndReplacesExisting() {
        MemberReviewCountPortStub port = new MemberReviewCountPortStub(List.of(
            MemberReviewCount.of(MemberId.of(11L), 30L, LocalDateTime.of(2026, 7, 20, 10, 0)),
            MemberReviewCount.of(MemberId.of(22L), 20L, LocalDateTime.of(2026, 7, 21, 10, 0)),
            MemberReviewCount.of(MemberId.of(33L), 10L, LocalDateTime.of(2026, 7, 22, 10, 0))
        ));
        MemberReviewRankRepositoryStub repository = new MemberReviewRankRepositoryStub();
        RankSettlementService service = new RankSettlementService(repository, port);

        int settled = service.settle(RankType.ALL, BASE_DATE, 10);

        assertThat(settled).isEqualTo(3);
        assertThat(repository.saved)
            .extracting(MemberReviewRank::getRankNo)
            .containsExactly(1, 2, 3);
        assertThat(repository.saved)
            .extracting(rank -> rank.getMemberId().value())
            .containsExactly(11L, 22L, 33L);
        assertThat(repository.saved)
            .extracting(MemberReviewRank::getReviewCount)
            .containsExactly(30, 20, 10);
    }

    @Test
    @DisplayName("적재 전에 같은 타입·기준일의 기존 랭킹을 먼저 삭제한다")
    void settle_deletesBeforeSaving() {
        MemberReviewCountPortStub port = new MemberReviewCountPortStub(List.of(
            MemberReviewCount.of(MemberId.of(11L), 5L, LocalDateTime.of(2026, 7, 20, 10, 0))
        ));
        MemberReviewRankRepositoryStub repository = new MemberReviewRankRepositoryStub();
        RankSettlementService service = new RankSettlementService(repository, port);

        service.settle(RankType.WEEKLY, BASE_DATE, 10);

        assertThat(repository.callOrder).containsExactly("delete", "saveAll");
        assertThat(repository.deletedRankType).isEqualTo(RankType.WEEKLY);
        assertThat(repository.deletedBaseDate).isEqualTo(BASE_DATE);
    }

    @Test
    @DisplayName("limit을 초과하는 상위 회원만 적재한다")
    void settle_appliesLimit() {
        List<MemberReviewCount> counts = new ArrayList<>();
        for (long i = 1; i <= 10; i++) {
            counts.add(MemberReviewCount.of(MemberId.of(i), 100L - i, LocalDateTime.of(2026, 7, 20, 10, 0)));
        }
        MemberReviewCountPortStub port = new MemberReviewCountPortStub(counts);
        MemberReviewRankRepositoryStub repository = new MemberReviewRankRepositoryStub();
        RankSettlementService service = new RankSettlementService(repository, port);

        int settled = service.settle(RankType.MONTHLY, BASE_DATE, 3);

        assertThat(settled).isEqualTo(3);
        assertThat(repository.saved)
            .extracting(rank -> rank.getMemberId().value())
            .containsExactly(1L, 2L, 3L);
    }

    @Test
    @DisplayName("집계할 리뷰가 없으면 기존 랭킹만 지우고 빈 목록을 적재한다")
    void settle_withNoReviews_clearsOnly() {
        MemberReviewCountPortStub port = new MemberReviewCountPortStub(List.of());
        MemberReviewRankRepositoryStub repository = new MemberReviewRankRepositoryStub();
        RankSettlementService service = new RankSettlementService(repository, port);

        int settled = service.settle(RankType.ALL, BASE_DATE, 10);

        assertThat(settled).isZero();
        assertThat(repository.saved).isEmpty();
        assertThat(repository.callOrder).containsExactly("delete", "saveAll");
    }

    @Test
    @DisplayName("ALL 타입은 서비스 개시 이전부터 기준일 끝까지를 집계 기간으로 쓴다")
    void settle_allType_usesAllTimePeriod() {
        MemberReviewCountPortStub port = new MemberReviewCountPortStub(List.of());
        RankSettlementService service = new RankSettlementService(new MemberReviewRankRepositoryStub(), port);

        service.settle(RankType.ALL, BASE_DATE, 10);

        assertThat(port.requestedStartAt).isEqualTo(LocalDateTime.of(2000, 1, 1, 0, 0, 0));
        assertThat(port.requestedEndAt).isEqualTo(LocalDateTime.of(BASE_DATE, LocalTime.MAX));
    }

    @Test
    @DisplayName("MONTHLY 타입은 기준일이 속한 달의 1일부터 말일까지를 집계 기간으로 쓴다")
    void settle_monthlyType_usesCalendarMonthPeriod() {
        MemberReviewCountPortStub port = new MemberReviewCountPortStub(List.of());
        RankSettlementService service = new RankSettlementService(new MemberReviewRankRepositoryStub(), port);

        service.settle(RankType.MONTHLY, BASE_DATE, 10);

        assertThat(port.requestedStartAt).isEqualTo(LocalDateTime.of(2026, 7, 1, 0, 0, 0));
        assertThat(port.requestedEndAt).isEqualTo(LocalDateTime.of(LocalDate.of(2026, 7, 31), LocalTime.MAX));
    }

    @Test
    @DisplayName("WEEKLY 타입은 기준일이 속한 주의 월요일부터 일요일까지를 집계 기간으로 쓴다")
    void settle_weeklyType_usesMondayToSundayPeriod() {
        MemberReviewCountPortStub port = new MemberReviewCountPortStub(List.of());
        RankSettlementService service = new RankSettlementService(new MemberReviewRankRepositoryStub(), port);

        service.settle(RankType.WEEKLY, BASE_DATE, 10);

        // 2026-07-30은 목요일 -> 그 주 월요일은 2026-07-27, 일요일은 2026-08-02
        assertThat(port.requestedStartAt).isEqualTo(LocalDateTime.of(2026, 7, 27, 0, 0, 0));
        assertThat(port.requestedEndAt).isEqualTo(LocalDateTime.of(LocalDate.of(2026, 8, 2), LocalTime.MAX));
    }

    @Test
    @DisplayName("settleAll은 전체·월간·주간 세 타입을 모두 집계한다")
    void settleAll_settlesEveryRankType() {
        MemberReviewCountPortStub port = new MemberReviewCountPortStub(List.of(
            MemberReviewCount.of(MemberId.of(11L), 5L, LocalDateTime.of(2026, 7, 20, 10, 0))
        ));
        MemberReviewRankRepositoryStub repository = new MemberReviewRankRepositoryStub();
        RankSettlementService service = new RankSettlementService(repository, port);

        int settled = service.settleAll(BASE_DATE);

        assertThat(settled).isEqualTo(3); // 타입당 1건
        assertThat(repository.deletedRankTypes)
            .containsExactly(RankType.ALL, RankType.MONTHLY, RankType.WEEKLY);
    }

    private static final class MemberReviewCountPortStub implements MemberReviewCountPort {

        private final List<MemberReviewCount> counts;
        private LocalDateTime requestedStartAt;
        private LocalDateTime requestedEndAt;

        private MemberReviewCountPortStub(List<MemberReviewCount> counts) {
            this.counts = counts;
        }

        @Override
        public List<MemberReviewCount> countReviewsByMemberWithPeriod(LocalDateTime startDate, LocalDateTime endDate) {
            this.requestedStartAt = startDate;
            this.requestedEndAt = endDate;
            return counts;
        }
    }

    private static final class MemberReviewRankRepositoryStub implements MemberReviewRankRepository {

        private final List<String> callOrder = new ArrayList<>();
        private final List<RankType> deletedRankTypes = new ArrayList<>();
        private List<MemberReviewRank> saved = List.of();
        private RankType deletedRankType;
        private LocalDate deletedBaseDate;

        @Override
        public Optional<MemberReviewRank> findLatestByMemberIdAndRankType(MemberId memberId, RankType rankType) {
            return Optional.empty();
        }

        @Override
        public void saveAll(List<MemberReviewRank> ranks) {
            callOrder.add("saveAll");
            saved = List.copyOf(ranks);
        }

        @Override
        public void deleteByRankTypeAndBaseDate(RankType rankType, LocalDate baseDate) {
            callOrder.add("delete");
            deletedRankTypes.add(rankType);
            deletedRankType = rankType;
            deletedBaseDate = baseDate;
        }
    }
}
