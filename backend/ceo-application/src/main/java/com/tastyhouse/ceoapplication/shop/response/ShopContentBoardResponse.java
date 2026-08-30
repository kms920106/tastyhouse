package com.tastyhouse.ceoapplication.shop.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가게 콘텐츠보드 응답")
public record ShopContentBoardResponse(
    @Schema(description = "콘텐츠보드 ID", example = "1")
    Long id,

    @Schema(description = "가게 ID", example = "1")
    Long shopId,

    @Schema(description = "콘텐츠 형태", example = "IMAGE", allowableValues = {"IMAGE", "GIF", "VIDEO"})
    String contentType,

    @Schema(description = "콘텐츠 주제", example = "EXTERIOR", allowableValues = {"EXTERIOR", "INTERIOR", "FOOD_STORY", "NEWS"})
    String topic,

    @Schema(description = "이미지 URL (IMAGE/GIF인 경우, 없으면 null)", example = "https://firebasestorage.googleapis.com/v0/b/bucket/o/2025%2F02%2F16%2Fuuid.jpg?alt=media")
    String imageUrl,

    @Schema(description = "유튜브 영상 URL (VIDEO인 경우)", example = "https://www.youtube.com/watch?v=abcdefg")
    String youtubeUrl,

    @Schema(description = "설명", example = "매장 외부 전경입니다.")
    String description,

    @Schema(description = "숨김 여부", example = "false")
    boolean hidden
) {
    public static ShopContentBoardResponse of(
        Long id,
        Long shopId,
        String contentType,
        String topic,
        String imageUrl,
        String youtubeUrl,
        String description,
        boolean hidden
    ) {
        return new ShopContentBoardResponse(
            id,
            shopId,
            contentType,
            topic,
            imageUrl,
            youtubeUrl,
            description,
            hidden
        );
    }
}
