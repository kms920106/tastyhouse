package com.tastyhouse.webapi.event.response;

public record PrizeItem(
    Long id,
    Integer prizeRank,
    String name,
    String brand,
    String imageUrl
) {
    public static PrizeItem from(
        Long id,
        Integer prizeRank,
        String name,
        String brand,
        String imageUrl
    ) {
        return new PrizeItem(
            id,
            prizeRank,
            name,
            brand,
            imageUrl
        );
    }
}
