package com.tastyhouse.application.crawling.bbq.port.out;

import java.util.List;

public record BbqProductSubOptionResponse(
    Long id,
    String subOptionTitle,
    Integer requiredSelectCount,
    Integer maxSelectCount,
    List<SubOptionItemDetailResponse> subOptionItemDetailResponseList
) {
    public static BbqProductSubOptionResponse from(
        Long id,
        String subOptionTitle,
        Integer requiredSelectCount,
        Integer maxSelectCount,
        List<SubOptionItemDetailResponse> subOptionItemDetailResponseList
    ) {
        return new BbqProductSubOptionResponse(
            id,
            subOptionTitle,
            requiredSelectCount,
            maxSelectCount,
            subOptionItemDetailResponseList
        );
    }
}
