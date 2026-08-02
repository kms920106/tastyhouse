package com.tastyhouse.adminapi.shop.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가게 콘텐츠보드 검수 목록 항목 응답")
public record ShopContentBoardListItemResponse(
    @Schema(description = "콘텐츠보드 ID", example = "1")
    Long id,

    @Schema(description = "가게 ID", example = "1")
    Long shopId,

    @Schema(description = "콘텐츠 형태", example = "IMAGE", allowableValues = {"IMAGE", "GIF", "VIDEO"})
    String contentType,

    @Schema(description = "콘텐츠 주제", example = "EXTERIOR", allowableValues = {"EXTERIOR", "INTERIOR", "FOOD_STORY", "NEWS"})
    String topic,

    @Schema(description = "이미지 URL(IMAGE/GIF인 경우, 파일이 없으면 null)", example = "https://firebasestorage.googleapis.com/v0/b/bucket/o/2025%2F02%2F16%2Fcontent.png?alt=media")
    String imageUrl,

    @Schema(description = "유튜브 영상 URL (VIDEO인 경우)", example = "https://www.youtube.com/watch?v=abcdefg")
    String youtubeUrl,

    @Schema(description = "설명", example = "매장 외부 전경입니다.")
    String description,

    @Schema(description = "숨김 여부", example = "false")
    boolean hidden,

    @Schema(description = "생성 일시", example = "2026-07-25T10:00:00")
    LocalDateTime createdAt
) {
    public static ShopContentBoardListItemResponse of(
        Long id,
        Long shopId,
        String contentType,
        String topic,
        String imageUrl,
        String youtubeUrl,
        String description,
        boolean hidden,
        LocalDateTime createdAt
    ) {
        return new ShopContentBoardListItemResponse(
            id,
            shopId,
            contentType,
            topic,
            imageUrl,
            youtubeUrl,
            description,
            hidden,
            createdAt
        );
    }
}
