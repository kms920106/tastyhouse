package com.tastyhouse.core.entity.event.dto;

public record PrizeItemDto(
    Long id,
    Integer prizeRank,
    String name,
    String brand,
    String imageFilePath
) {
}
