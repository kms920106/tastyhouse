package com.tastyhouse.application.crawling.bbq.port.out;

public record SubOptionItemDetailResponse(
    Long id,
    String itemTitle,
    Integer addPrice,
    boolean soldOut,
    boolean hidden
) {
    public static SubOptionItemDetailResponse from(
        Long id,
        String itemTitle,
        Integer addPrice,
        boolean soldOut,
        boolean hidden
    ) {
        return new SubOptionItemDetailResponse(
            id,
            itemTitle,
            addPrice,
            soldOut,
            hidden
        );
    }
}
