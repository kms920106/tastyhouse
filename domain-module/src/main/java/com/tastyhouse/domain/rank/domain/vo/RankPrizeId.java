package com.tastyhouse.domain.rank.domain.vo;

public record RankPrizeId(Long value) {

    public RankPrizeId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("RankPrizeId는 양수여야 합니다: " + value);
        }
    }

    public static RankPrizeId of(Long value) {
        return new RankPrizeId(value);
    }
}
