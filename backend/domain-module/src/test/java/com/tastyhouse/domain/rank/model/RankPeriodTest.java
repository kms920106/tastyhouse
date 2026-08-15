package com.tastyhouse.domain.rank.model;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.rank.vo.RankPeriodId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다
 * (도메인/JPA 엔티티 분리로 얻는 테스트 용이성의 레퍼런스).
 */
class RankPeriodTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자·감사시각 없음)이고 삭제되지 않은 상태다")
    void of_createsTransientRankPeriod() {
        LocalDateTime startAt = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime endAt = LocalDateTime.of(2026, 1, 31, 23, 59);

        RankPeriod period = RankPeriod.of(startAt, endAt);

        assertThat(period.getId()).isNull();
        assertThat(period.getStartAt()).isEqualTo(startAt);
        assertThat(period.getEndAt()).isEqualTo(endAt);
        assertThat(period.isVisible()).isTrue();
        assertThat(period.isDeleted()).isFalse();
        assertThat(period.getCreatedAt()).isNull();
        assertThat(period.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("update는 시작·종료 일시·노출여부를 변경한다")
    void update_changesFields() {
        RankPeriod period = RankPeriod.of(LocalDateTime.of(2026, 1, 1, 0, 0), LocalDateTime.of(2026, 1, 31, 23, 59));

        LocalDateTime newStartAt = LocalDateTime.of(2026, 2, 1, 0, 0);
        LocalDateTime newEndAt = LocalDateTime.of(2026, 2, 28, 23, 59);
        period.update(newStartAt, newEndAt, false);

        assertThat(period.getStartAt()).isEqualTo(newStartAt);
        assertThat(period.getEndAt()).isEqualTo(newEndAt);
        assertThat(period.isVisible()).isFalse();
    }

    @Test
    @DisplayName("delete는 삭제 플래그를 true로 만든다(soft delete)")
    void delete_marksDeleted() {
        RankPeriod period = RankPeriod.of(LocalDateTime.of(2026, 1, 1, 0, 0), LocalDateTime.of(2026, 1, 31, 23, 59));

        period.delete();

        assertThat(period.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자·감사시각을 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        LocalDateTime startAt = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime endAt = LocalDateTime.of(2026, 1, 31, 23, 59);
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 2, 0, 0);

        RankPeriod period = RankPeriod.reconstitute(1L, startAt, endAt, true, false, createdAt, updatedAt);

        assertThat(period.getId()).isEqualTo(1L);
        assertThat(period.getRankPeriodId()).isEqualTo(RankPeriodId.of(1L));
        assertThat(period.getCreatedAt()).isEqualTo(createdAt);
        assertThat(period.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("미영속 상태에서 getRankPeriodId를 호출하면 RankPeriodId 불변식 위반으로 예외가 발생한다")
    void getRankPeriodId_onTransient_throws() {
        RankPeriod period = RankPeriod.of(LocalDateTime.of(2026, 1, 1, 0, 0), LocalDateTime.of(2026, 1, 31, 23, 59));

        assertThatThrownBy(period::getRankPeriodId)
            .isInstanceOf(IllegalArgumentException.class);
    }
}
