package com.tastyhouse.domain.rank.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.tastyhouse.domain.rank.model.MemberReviewRank;
import com.tastyhouse.domain.rank.model.RankType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.member.vo.MemberId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다
 * (도메인/JPA 엔티티 분리로 얻는 테스트 용이성의 레퍼런스). 상태전이·삭제가 없는 insert-only
 * 애그리거트이므로 생성·재구성만 검증한다.
 */
class MemberReviewRankTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자·감사시각 없음)이고 전달된 필드가 그대로 세팅된다")
    void of_createsTransientMemberReviewRank() {
        MemberId memberId = MemberId.of(1L);
        LocalDate baseDate = LocalDate.of(2026, 1, 1);
        LocalDateTime lastReviewAt = LocalDateTime.of(2026, 1, 1, 12, 0);

        MemberReviewRank rank = MemberReviewRank.of(memberId, 10, 1, RankType.MONTHLY, baseDate, lastReviewAt);

        assertThat(rank.getId()).isNull();
        assertThat(rank.getMemberId()).isEqualTo(memberId);
        assertThat(rank.getReviewCount()).isEqualTo(10);
        assertThat(rank.getRankNo()).isEqualTo(1);
        assertThat(rank.getRankType()).isEqualTo(RankType.MONTHLY);
        assertThat(rank.getBaseDate()).isEqualTo(baseDate);
        assertThat(rank.getLastReviewAt()).isEqualTo(lastReviewAt);
        assertThat(rank.getCreatedAt()).isNull();
        assertThat(rank.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자·감사시각을 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        MemberId memberId = MemberId.of(1L);
        LocalDate baseDate = LocalDate.of(2026, 1, 1);
        LocalDateTime lastReviewAt = LocalDateTime.of(2026, 1, 1, 12, 0);
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 2, 0, 0);

        MemberReviewRank rank = MemberReviewRank.reconstitute(
            1L, memberId, 10, 1, RankType.MONTHLY, baseDate, lastReviewAt, createdAt, updatedAt
        );

        assertThat(rank.getId()).isEqualTo(1L);
        assertThat(rank.getMemberId()).isEqualTo(memberId);
        assertThat(rank.getCreatedAt()).isEqualTo(createdAt);
        assertThat(rank.getUpdatedAt()).isEqualTo(updatedAt);
    }
}
