package com.tastyhouse.application.rank.port.out;

public record RankPrizeManagementResult(
    Long id,
    Long periodId,
    Integer prizeRank,
    String name,
    String brand,
    Long imageFileId,
    String imageFileName,
    String imageUrl
) {
}
