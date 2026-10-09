package com.tastyhouse.infrastructure.jpa.rank.persistence;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.rank.model.MemberReviewRank;
import com.tastyhouse.domain.rank.model.RankPeriod;
import com.tastyhouse.domain.rank.model.RankPrize;
import com.tastyhouse.domain.rank.model.RankType;
import com.tastyhouse.domain.rank.vo.RankPeriodId;

import static org.assertj.core.api.Assertions.assertThat;

class RankMapperTest {

    @Test
    @DisplayName("MemberReviewRank → 엔티티 변환 시 모든 컬럼 값이 보존된다")
    void memberReviewRankToEntity() {
        MemberReviewRankJpaEntity entity = MemberReviewRankMapper.toEntity(memberReviewRank());

        assertThat(entity.getMemberId()).isEqualTo(32L);
        assertThat(entity.getReviewCount()).isEqualTo(17);
        assertThat(entity.getRankNo()).isEqualTo(3);
        assertThat(entity.getRankType()).isEqualTo("MONTHLY");
        assertThat(entity.getBaseDate()).isEqualTo(LocalDate.of(2026, 5, 6));
        assertThat(entity.getLastReviewAt()).isEqualTo(LocalDateTime.of(2026, 5, 5, 10, 0));
    }

    @Test
    @DisplayName("엔티티 → MemberReviewRank 변환 시 id·생성·수정 시각을 포함한 모든 필드가 보존된다")
    void memberReviewRankToDomain() {
        MemberReviewRank original = memberReviewRank();
        MemberReviewRankJpaEntity entity = MemberReviewRankMapper.toEntity(original);
        ReflectionTestUtils.setField(entity, "id", 31L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 5, 6, 1, 0));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 5, 6, 2, 0));

        MemberReviewRank restored = MemberReviewRankMapper.toDomain(entity);

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("RankPeriod → 엔티티 변환 시 모든 컬럼 값이 보존된다")
    void rankPeriodToEntity() {
        RankPeriodJpaEntity entity = RankPeriodMapper.toEntity(rankPeriod());

        assertThat(entity.getStartAt()).isEqualTo(LocalDateTime.of(2026, 1, 1, 0, 0));
        assertThat(entity.getEndAt()).isEqualTo(LocalDateTime.of(2026, 1, 31, 23, 59));
        assertThat(entity.isVisible()).isTrue();
        assertThat(entity.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("엔티티 → RankPeriod 변환 시 id·생성·수정 시각을 포함한 모든 필드가 보존된다")
    void rankPeriodToDomain() {
        RankPeriod original = rankPeriod();
        RankPeriodJpaEntity entity = RankPeriodMapper.toEntity(original);
        ReflectionTestUtils.setField(entity, "id", 41L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2025, 12, 1, 0, 0));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2025, 12, 2, 0, 0));

        RankPeriod restored = RankPeriodMapper.toDomain(entity);

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("RankPrize → 엔티티 변환 시 모든 컬럼 값이 보존된다")
    void rankPrizeToEntity() {
        RankPrizeJpaEntity entity = RankPrizeMapper.toEntity(rankPrize());

        assertThat(entity.getRankId()).isEqualTo(52L);
        assertThat(entity.getPrizeRank()).isEqualTo(2);
        assertThat(entity.getName()).isEqualTo("경품명");
        assertThat(entity.getBrand()).isEqualTo("브랜드");
        assertThat(entity.getImageFileId()).isEqualTo(53L);
        assertThat(entity.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("엔티티 → RankPrize 변환 시 id·생성·수정 시각을 포함한 모든 필드가 보존된다")
    void rankPrizeToDomain() {
        RankPrize original = rankPrize();
        RankPrizeJpaEntity entity = RankPrizeMapper.toEntity(original);
        ReflectionTestUtils.setField(entity, "id", 51L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 2, 1, 0, 0));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 2, 2, 0, 0));

        RankPrize restored = RankPrizeMapper.toDomain(entity);

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    private static MemberReviewRank memberReviewRank() {
        return MemberReviewRank.reconstitute(
            31L, MemberId.of(32L), 17, 3, RankType.MONTHLY,
            LocalDate.of(2026, 5, 6),
            LocalDateTime.of(2026, 5, 5, 10, 0),
            LocalDateTime.of(2026, 5, 6, 1, 0),
            LocalDateTime.of(2026, 5, 6, 2, 0));
    }

    private static RankPeriod rankPeriod() {
        return RankPeriod.reconstitute(
            41L,
            LocalDateTime.of(2026, 1, 1, 0, 0),
            LocalDateTime.of(2026, 1, 31, 23, 59),
            true, false,
            LocalDateTime.of(2025, 12, 1, 0, 0),
            LocalDateTime.of(2025, 12, 2, 0, 0));
    }

    private static RankPrize rankPrize() {
        return RankPrize.reconstitute(
            51L, RankPeriodId.of(52L), 2, "경품명", "브랜드", UploadedFileId.of(53L), true,
            LocalDateTime.of(2026, 2, 1, 0, 0),
            LocalDateTime.of(2026, 2, 2, 0, 0));
    }
}
