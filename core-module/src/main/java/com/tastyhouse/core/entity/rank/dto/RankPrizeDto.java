package com.tastyhouse.core.entity.rank.dto;

public record RankPrizeDto(
    Long id,
    Integer prizeRank,
    String name,
    String brand,
    String imageFilePath
) {
}
