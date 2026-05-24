package com.tastyhouse.core.domain.rank.application.dto.result;

public record RankPrizeResult(
    Long id,
    Integer prizeRank,
    String name,
    String brand,
    String imageFilePath
) {
}
