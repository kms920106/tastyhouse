package com.tastyhouse.webapi.rank.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "랭킹 경품 응답")
public record RankPrizeListItemResponse(
    @Schema(description = "경품 ID", example = "1")
    Long id,

    @Schema(description = "순위", example = "1")
    Integer prizeRank,

    @Schema(description = "경품명", example = "아이패드 Pro 11인치")
    String name,

    @Schema(description = "브랜드", example = "Apple")
    String brand,

    @Schema(description = "경품 이미지 URL", example = "https://example.com/prize.jpg")
    String imageUrl
) {
    public static RankPrizeListItemResponse from(
        Long id,
        Integer prizeRank,
        String name,
        String brand,
        String imageUrl
    ) {
        return new RankPrizeListItemResponse(id, prizeRank, name, brand, imageUrl);
    }
}
