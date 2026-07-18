package com.tastyhouse.core.domain.rank.application.dto.command;

public record RankPrizeCreateCommand(
    Integer prizeRank,
    String name,
    String brand,
    Long imageFileId
) {

    public static RankPrizeCreateCommand of(
        Integer prizeRank,
        String name,
        String brand,
        Long imageFileId
    ) {
        return new RankPrizeCreateCommand(prizeRank, name, brand, imageFileId);
    }
}
