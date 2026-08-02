package com.tastyhouse.domain.rank.vo;

public record RankPeriodId(Long value) {

    public RankPeriodId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("RankPeriodId는 양수여야 합니다: " + value);
        }
    }

    public static RankPeriodId of(Long value) {
        return new RankPeriodId(value);
    }
}
