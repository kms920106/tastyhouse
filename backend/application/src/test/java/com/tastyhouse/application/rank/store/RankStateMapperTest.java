package com.tastyhouse.application.rank.store;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.rank.model.MemberReviewRank;
import com.tastyhouse.domain.rank.model.RankPeriod;
import com.tastyhouse.domain.rank.model.RankPrize;
import com.tastyhouse.domain.rank.model.RankType;
import com.tastyhouse.domain.rank.vo.RankPeriodId;

import static org.assertj.core.api.Assertions.assertThat;

class RankStateMapperTest {

    @Test
    @DisplayName("MemberReviewRank → MemberReviewRankState → MemberReviewRank 왕복 시 모든 필드가 보존된다")
    void memberReviewRankRoundTrip() {
        MemberReviewRank original = MemberReviewRank.reconstitute(
            31L, MemberId.of(32L), 17, 3, RankType.MONTHLY,
            LocalDate.of(2026, 5, 6),
            LocalDateTime.of(2026, 5, 5, 10, 0),
            LocalDateTime.of(2026, 5, 6, 1, 0),
            LocalDateTime.of(2026, 5, 6, 2, 0));

        MemberReviewRank restored = MemberReviewRankStateMapper.toDomain(MemberReviewRankStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("RankPeriod → RankPeriodState → RankPeriod 왕복 시 모든 필드가 보존된다")
    void rankPeriodRoundTrip() {
        RankPeriod original = RankPeriod.reconstitute(
            41L,
            LocalDateTime.of(2026, 1, 1, 0, 0),
            LocalDateTime.of(2026, 1, 31, 23, 59),
            true, false,
            LocalDateTime.of(2025, 12, 1, 0, 0),
            LocalDateTime.of(2025, 12, 2, 0, 0));

        RankPeriod restored = RankPeriodStateMapper.toDomain(RankPeriodStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("RankPrize → RankPrizeState → RankPrize 왕복 시 모든 필드가 보존된다")
    void rankPrizeRoundTrip() {
        RankPrize original = RankPrize.reconstitute(
            51L, RankPeriodId.of(52L), 2, "경품명", "브랜드", UploadedFileId.of(53L), true,
            LocalDateTime.of(2026, 2, 1, 0, 0),
            LocalDateTime.of(2026, 2, 2, 0, 0));

        RankPrize restored = RankPrizeStateMapper.toDomain(RankPrizeStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
