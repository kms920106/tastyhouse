package com.tastyhouse.core.domain.rank.application.dto.command;

public record RankPrizeUpdateCommand(
    Integer prizeRank,
    String name,
    String brand,
    Long imageFileId
) {

    public static RankPrizeUpdateCommand of(
        Integer prizeRank,
        String name,
        String brand,
        Long imageFileId
    ) {
        return new RankPrizeUpdateCommand(prizeRank, name, brand, imageFileId);
    }
}
