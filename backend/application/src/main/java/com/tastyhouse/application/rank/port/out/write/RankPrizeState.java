package com.tastyhouse.application.rank.port.out.write;

import java.time.LocalDateTime;

public record RankPrizeState(
    Long id,
    Long rankId,
    Integer prizeRank,
    String name,
    String brand,
    Long imageFileId,
    boolean deleted,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
