package com.tastyhouse.adminapi.shop.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "점주 공지 검수 목록 항목 응답")
public record ShopNoticeManagementListItemResponse(
    @Schema(description = "공지 ID", example = "12")
    Long id,

    @Schema(description = "가게 ID", example = "3")
    Long shopId,

    @Schema(description = "가게명", example = "맛있는집 강남점")
    String shopName,

    @Schema(description = "공지 본문", example = "이번 주 신메뉴 출시했습니다.")
    String content,

    @Schema(description = "첨부 이미지 URL 목록 (등록 순, 없으면 빈 배열)")
    List<String> imageUrls,

    @Schema(description = "앱 노출 여부", example = "true")
    boolean exposed,

    @Schema(description = "게시중단 여부", example = "false")
    boolean hidden,

    @Schema(description = "생성 일시", example = "2026-08-15T10:00:00")
    LocalDateTime createdAt
) {
    public static ShopNoticeManagementListItemResponse of(
        Long id,
        Long shopId,
        String shopName,
        String content,
        List<String> imageUrls,
        boolean exposed,
        boolean hidden,
        LocalDateTime createdAt
    ) {
        return new ShopNoticeManagementListItemResponse(
            id,
            shopId,
            shopName,
            content,
            imageUrls,
            exposed,
            hidden,
            createdAt
        );
    }
}
