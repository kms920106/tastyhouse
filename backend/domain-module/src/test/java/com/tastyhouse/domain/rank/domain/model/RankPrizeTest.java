package com.tastyhouse.domain.rank.domain.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.rank.model.RankPrize;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.rank.vo.RankPeriodId;
import com.tastyhouse.domain.rank.vo.RankPrizeId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다
 * (도메인/JPA 엔티티 분리로 얻는 테스트 용이성의 레퍼런스).
 */
class RankPrizeTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자·감사시각 없음)이고 삭제되지 않은 상태다")
    void of_createsTransientRankPrize() {
        RankPrize prize = RankPrize.of(RankPeriodId.of(1L), 1, "경품명", "브랜드", UploadedFileId.of(10L));

        assertThat(prize.getId()).isNull();
        assertThat(prize.getRankId()).isEqualTo(RankPeriodId.of(1L));
        assertThat(prize.getPrizeRank()).isEqualTo(1);
        assertThat(prize.getName()).isEqualTo("경품명");
        assertThat(prize.getBrand()).isEqualTo("브랜드");
        assertThat(prize.getImageFileId()).isEqualTo(UploadedFileId.of(10L));
        assertThat(prize.isDeleted()).isFalse();
        assertThat(prize.getCreatedAt()).isNull();
        assertThat(prize.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("update는 순위·이름·브랜드·이미지파일ID를 변경하고 rankId는 보존한다")
    void update_changesFieldsAndPreservesRankId() {
        RankPrize prize = RankPrize.of(RankPeriodId.of(1L), 1, "경품명", "브랜드", UploadedFileId.of(10L));

        prize.update(2, "새 경품명", "새 브랜드", UploadedFileId.of(20L));

        assertThat(prize.getRankId()).isEqualTo(RankPeriodId.of(1L));
        assertThat(prize.getPrizeRank()).isEqualTo(2);
        assertThat(prize.getName()).isEqualTo("새 경품명");
        assertThat(prize.getBrand()).isEqualTo("새 브랜드");
        assertThat(prize.getImageFileId()).isEqualTo(UploadedFileId.of(20L));
    }

    @Test
    @DisplayName("delete는 삭제 플래그를 true로 만든다(soft delete)")
    void delete_marksDeleted() {
        RankPrize prize = RankPrize.of(RankPeriodId.of(1L), 1, "경품명", "브랜드", UploadedFileId.of(10L));

        prize.delete();

        assertThat(prize.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자·감사시각을 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 2, 0, 0);

        RankPrize prize = RankPrize.reconstitute(
            1L, RankPeriodId.of(2L), 1, "경품명", "브랜드", UploadedFileId.of(10L), false, createdAt, updatedAt
        );

        assertThat(prize.getId()).isEqualTo(1L);
        assertThat(prize.getRankPrizeId()).isEqualTo(RankPrizeId.of(1L));
        assertThat(prize.getCreatedAt()).isEqualTo(createdAt);
        assertThat(prize.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("미영속 상태에서 getRankPrizeId를 호출하면 RankPrizeId 불변식 위반으로 예외가 발생한다")
    void getRankPrizeId_onTransient_throws() {
        RankPrize prize = RankPrize.of(RankPeriodId.of(1L), 1, "경품명", "브랜드", UploadedFileId.of(10L));

        assertThatThrownBy(prize::getRankPrizeId)
            .isInstanceOf(IllegalArgumentException.class);
    }
}
