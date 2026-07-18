package com.tastyhouse.core.domain.rank.application.dto.result;

public record RankPrizeManagementResult(
    Long id,
    Long periodId,
    Integer prizeRank,
    String name,
    String brand,
    Long imageFileId,
    String imageFileName,
    String imageFilePath
) {
}
