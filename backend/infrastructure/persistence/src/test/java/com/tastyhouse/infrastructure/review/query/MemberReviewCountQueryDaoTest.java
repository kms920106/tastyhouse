package com.tastyhouse.infrastructure.review.query;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.application.menureview.port.out.MenuReviewMemberCountResult;

import static org.assertj.core.api.Assertions.assertThat;

class MemberReviewCountQueryDaoTest {
    @Test
    @DisplayName("매장 리뷰만 있는 회원의 집계는 그대로 유지된다")
    void mergeAndSort_keepsReviewOnlyMember() {
        List<MemberReviewCountResult> merged = MemberReviewCountQueryDao.mergeAndSort(
            List.of(reviewCount(1L, 3L, at(10))),
            List.of()
        );

        assertThat(merged).singleElement().satisfies(row -> {
            assertThat(row.memberId()).isEqualTo(1L);
            assertThat(row.reviewCount()).isEqualTo(3L);
            assertThat(row.lastReviewAt()).isEqualTo(at(10));
        });
    }

    @Test
    @DisplayName("메뉴 평가만 있는 회원도 합산 결과에 포함된다")
    void mergeAndSort_includesMenuReviewOnlyMember() {
        List<MemberReviewCountResult> merged = MemberReviewCountQueryDao.mergeAndSort(
            List.of(),
            List.of(menuReviewCount(2L, 4L, at(11)))
        );

        assertThat(merged).singleElement().satisfies(row -> {
            assertThat(row.memberId()).isEqualTo(2L);
            assertThat(row.reviewCount()).isEqualTo(4L);
            assertThat(row.lastReviewAt()).isEqualTo(at(11));
        });
    }

    @Test
    @DisplayName("둘 다 있는 회원은 건수를 더하고 마지막 작성 시각은 더 늦은 쪽을 취한다")
    void mergeAndSort_sumsCountsAndTakesLatestTimestamp() {
        List<MemberReviewCountResult> merged = MemberReviewCountQueryDao.mergeAndSort(
            List.of(reviewCount(3L, 2L, at(9))),
            List.of(menuReviewCount(3L, 5L, at(15)))
        );

        assertThat(merged).singleElement().satisfies(row -> {
            assertThat(row.reviewCount()).isEqualTo(7L);
            assertThat(row.lastReviewAt()).isEqualTo(at(15));
        });
    }

    @Test
    @DisplayName("메뉴 평가 쪽이 더 이르면 매장 리뷰 시각이 남는다(더 늦은 쪽 유지)")
    void mergeAndSort_keepsReviewTimestampWhenItIsLater() {
        List<MemberReviewCountResult> merged = MemberReviewCountQueryDao.mergeAndSort(
            List.of(reviewCount(4L, 1L, at(20))),
            List.of(menuReviewCount(4L, 1L, at(8)))
        );

        assertThat(merged).singleElement()
            .satisfies(row -> assertThat(row.lastReviewAt()).isEqualTo(at(20)));
    }

    @Test
    @DisplayName("건수 내림차순 → 마지막 작성 이른 순 → 회원 ID 순으로 정렬한다")
    void mergeAndSort_appliesRankingOrder() {
        List<MemberReviewCountResult> merged = MemberReviewCountQueryDao.mergeAndSort(
            List.of(
                reviewCount(10L, 2L, at(12)),
                reviewCount(11L, 2L, at(9)),
                reviewCount(12L, 2L, at(9)),
                reviewCount(13L, 1L, at(8))
            ),
            List.of(menuReviewCount(13L, 4L, at(7)))
        );

        assertThat(merged).extracting(MemberReviewCountResult::memberId)
            .containsExactly(13L, 11L, 12L, 10L);
    }

    @Test
    @DisplayName("합산 후에 순위가 뒤바뀐다 — 메뉴 평가를 빠뜨리면 잡히지 않는 회귀")
    void mergeAndSort_menuReviewChangesRanking() {
        List<MemberReviewCountResult> merged = MemberReviewCountQueryDao.mergeAndSort(
            List.of(
                reviewCount(20L, 5L, at(10)),
                reviewCount(21L, 1L, at(10))
            ),
            List.of(menuReviewCount(21L, 9L, at(11)))
        );

        assertThat(merged).extracting(MemberReviewCountResult::memberId)
            .containsExactly(21L, 20L);
    }

    private MemberReviewCountResult reviewCount(Long memberId, Long count, LocalDateTime lastAt) {
        return new MemberReviewCountResult(memberId, count, lastAt);
    }

    private MenuReviewMemberCountResult menuReviewCount(Long memberId, Long count, LocalDateTime lastAt) {
        return new MenuReviewMemberCountResult(memberId, count, lastAt);
    }

    private LocalDateTime at(int hour) {
        return LocalDateTime.of(2026, 6, 1, hour, 0);
    }
}
