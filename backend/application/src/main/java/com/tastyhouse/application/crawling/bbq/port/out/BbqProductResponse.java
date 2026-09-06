package com.tastyhouse.application.crawling.bbq.port.out;

public record BbqProductResponse(
    Long id,
    String name,
    String description,
    String imageUrl,
    Integer originalPrice,
    Integer addPrice,
    boolean soldOut,
    boolean adultOnly,
    boolean canDeliver,
    boolean canTakeout
) {
    public static BbqProductResponse from(
        Long id,
        String name,
        String description,
        String imageUrl,
        Integer originalPrice,
        Integer addPrice,
        boolean soldOut,
        boolean adultOnly,
        boolean canDeliver,
        boolean canTakeout
    ) {
        return new BbqProductResponse(
            id,
            name,
            description,
            imageUrl,
            originalPrice,
            addPrice,
            soldOut,
            adultOnly,
            canDeliver,
            canTakeout
        );
    }
}
