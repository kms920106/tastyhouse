package com.tastyhouse.application.rank.port.out;

public record RankPrizeResult(
    Long id,
    Integer prizeRank,
    String name,
    String brand,
    String imageUrl
) {
}
