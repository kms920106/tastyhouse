package com.tastyhouse.domain.member.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.domain.member.model.MemberGrade;
import com.tastyhouse.domain.member.model.MemberStatus;
import com.tastyhouse.domain.member.port.MemberReviewCount;
import com.tastyhouse.domain.member.port.MemberReviewCountPort;
import com.tastyhouse.domain.member.repository.MemberRepository;
import com.tastyhouse.domain.member.vo.MemberId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 회원 등급 확정 도메인 서비스 단위 테스트.
 *
 * <p>순수 POJO(도메인 서비스)이므로 Spring 컨텍스트·JPA 없이 조회 포트와 write 포트를 손으로 만든
 * fake로 대체해 검증한다({@code RankSettlementServiceTest}·{@code MailVerificationServiceTest} 선례).
 */
class GradeSettlementServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 15, 3, 30);

    @Test
    @DisplayName("등급 경계값 직전·정확값·직후 리뷰 수를 각각 알맞은 등급으로 판정한다")
    void settleAll_assignsGradeAtBoundaries() {
        MemberReviewCountPortFake port = new MemberReviewCountPortFake(List.of(
            reviewCount(1L, 0L),      // NEWCOMER 하한
            reviewCount(2L, 99L),     // ACTIVE 직전 → NEWCOMER
            reviewCount(3L, 100L),    // ACTIVE 정확값
            reviewCount(4L, 499L),    // INSIDER 직전 → ACTIVE
            reviewCount(5L, 500L),    // INSIDER 정확값
            reviewCount(6L, 700L),    // GOURMET 정확값
            reviewCount(7L, 999L),    // TEHA 직전 → GOURMET
            reviewCount(8L, 1000L),   // TEHA 정확값
            reviewCount(9L, 5000L)    // TEHA 상한 없음
        ));
        MemberRepositoryFake repository = new MemberRepositoryFake();
        GradeSettlementService service = new GradeSettlementService(port, repository);

        service.settleAll(NOW);

        assertThat(repository.updatedIdsByGrade.get(MemberGrade.NEWCOMER)).containsExactly(1L, 2L);
        assertThat(repository.updatedIdsByGrade.get(MemberGrade.ACTIVE)).containsExactly(3L, 4L);
        assertThat(repository.updatedIdsByGrade.get(MemberGrade.INSIDER)).containsExactly(5L);
        assertThat(repository.updatedIdsByGrade.get(MemberGrade.GOURMET)).containsExactly(6L, 7L);
        assertThat(repository.updatedIdsByGrade.get(MemberGrade.TEHA)).containsExactly(8L, 9L);
    }

    @Test
    @DisplayName("등급이 상승·유지·강등되는 회원이 섞여 있어도 리뷰 수 기준 등급으로 재산정한다")
    void settleAll_recalculatesRegardlessOfCurrentGrade() {
        MemberReviewCountPortFake port = new MemberReviewCountPortFake(List.of(
            reviewCount(11L, 500L),   // 상승: NEWCOMER → INSIDER
            reviewCount(22L, 100L),   // 유지: ACTIVE → ACTIVE
            reviewCount(33L, 10L)     // 강등: TEHA → NEWCOMER
        ));
        MemberRepositoryFake repository = new MemberRepositoryFake();
        GradeSettlementService service = new GradeSettlementService(port, repository);

        service.settleAll(NOW);

        assertThat(repository.updatedIdsByGrade.get(MemberGrade.INSIDER)).containsExactly(11L);
        assertThat(repository.updatedIdsByGrade.get(MemberGrade.ACTIVE)).containsExactly(22L);
        assertThat(repository.updatedIdsByGrade.get(MemberGrade.NEWCOMER)).containsExactly(33L);
    }

    @Test
    @DisplayName("전체 기간(2000-01-01 ~ 현재 시각)으로 리뷰 수를 조회한다")
    void settleAll_queriesAllTimePeriod() {
        MemberReviewCountPortFake port = new MemberReviewCountPortFake(List.of());
        GradeSettlementService service = new GradeSettlementService(port, new MemberRepositoryFake());

        service.settleAll(NOW);

        assertThat(port.requestedStartDate).isEqualTo(LocalDateTime.of(2000, 1, 1, 0, 0, 0));
        assertThat(port.requestedEndDate).isEqualTo(NOW);
    }

    @Test
    @DisplayName("갱신 대상이 없는 등급은 일괄 갱신을 호출하지 않고, 갱신된 회원 수를 합산해 반환한다")
    void settleAll_skipsEmptyGradesAndSumsUpdatedCount() {
        MemberReviewCountPortFake port = new MemberReviewCountPortFake(List.of(
            reviewCount(1L, 0L),
            reviewCount(2L, 1000L)
        ));
        MemberRepositoryFake repository = new MemberRepositoryFake();
        GradeSettlementService service = new GradeSettlementService(port, repository);

        long updated = service.settleAll(NOW);

        assertThat(updated).isEqualTo(2);
        assertThat(repository.updatedIdsByGrade.keySet())
            .containsExactlyInAnyOrder(MemberGrade.NEWCOMER, MemberGrade.TEHA);
    }

    @Test
    @DisplayName("리뷰 작성 회원이 없으면 아무 등급도 갱신하지 않는다")
    void settleAll_doesNothingWhenNoReviewCounts() {
        MemberRepositoryFake repository = new MemberRepositoryFake();
        GradeSettlementService service = new GradeSettlementService(
            new MemberReviewCountPortFake(List.of()), repository);

        long updated = service.settleAll(NOW);

        assertThat(updated).isZero();
        assertThat(repository.updatedIdsByGrade).isEmpty();
    }

    private static MemberReviewCount reviewCount(long memberId, long count) {
        return MemberReviewCount.of(MemberId.of(memberId), count);
    }

    private static class MemberReviewCountPortFake implements MemberReviewCountPort {

        private final List<MemberReviewCount> reviewCounts;

        private LocalDateTime requestedStartDate;
        private LocalDateTime requestedEndDate;

        private MemberReviewCountPortFake(List<MemberReviewCount> reviewCounts) {
            this.reviewCounts = reviewCounts;
        }

        @Override
        public List<MemberReviewCount> countReviewsByMemberWithPeriod(
            LocalDateTime startDate,
            LocalDateTime endDate
        ) {
            this.requestedStartDate = startDate;
            this.requestedEndDate = endDate;
            return reviewCounts;
        }
    }

    private static class MemberRepositoryFake implements MemberRepository {

        private final Map<MemberGrade, List<Long>> updatedIdsByGrade = new EnumMap<>(MemberGrade.class);

        @Override
        public long bulkUpdateGrade(List<Long> memberIds, MemberGrade grade) {
            updatedIdsByGrade.computeIfAbsent(grade, key -> new ArrayList<>()).addAll(memberIds);
            return memberIds.size();
        }

        @Override
        public Optional<Member> findById(MemberId memberId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Member> findByUsername(String username) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean existsByUsername(String username) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean existsByNickname(String nickname) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Member> findByNickname(String nickname) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean existsByPhoneNumberAndStatusNot(String phoneNumber, MemberStatus memberStatus) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Member> findByPhoneNumberAndStatusNot(String phoneNumber, MemberStatus memberStatus) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Member save(Member member) {
            throw new UnsupportedOperationException();
        }
    }
}
