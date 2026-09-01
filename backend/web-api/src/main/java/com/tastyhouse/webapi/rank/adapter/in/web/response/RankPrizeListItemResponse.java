package com.tastyhouse.webapi.rank.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.rank.port.out.RankPrizeResult;

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
    public static RankPrizeListItemResponse from(RankPrizeResult result) {
        return new RankPrizeListItemResponse(
            result.id(),
            result.prizeRank(),
            result.name(),
            result.brand(),
            result.imageUrl()
        );
    }
}
