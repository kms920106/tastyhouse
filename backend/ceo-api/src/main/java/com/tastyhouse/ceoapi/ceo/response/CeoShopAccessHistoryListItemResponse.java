package com.tastyhouse.ceoapi.ceo.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 내 시스템 접근권한 이력 목록 항목 응답.
 *
 * <p>{@code actorAdminId}는 노출하지 않는다 — 내부 식별자이며, {@code ShopChangeHistoryListItemResponse}가
 * {@code actorType}/{@code actorId}를 감춘 선례를 따른다.
 */
@Schema(description = "점주 시스템 접근권한 이력 목록 항목")
public record CeoShopAccessHistoryListItemResponse(

    @Schema(description = "이력 ID", example = "512")
    Long id,

    @Schema(description = "가게 ID", example = "12")
    Long shopId,

    @Schema(description = "가게 이름", example = "맛있는집 강남점")
    String shopName,

    @Schema(description = "조치 유형 코드", example = "GRANT", allowableValues = {"GRANT", "REVOKE"})
    String actionType,

    @Schema(description = "조치 유형 한글 라벨", example = "권한 부여")
    String actionTypeName,

    @Schema(description = "조치 시각", example = "2026-08-14T09:12:41")
    LocalDateTime occurredAt
) {

    public static CeoShopAccessHistoryListItemResponse from(
        Long id,
        Long shopId,
        String shopName,
        String actionType,
        String actionTypeName,
        LocalDateTime occurredAt
    ) {
        return new CeoShopAccessHistoryListItemResponse(
            id,
            shopId,
            shopName,
            actionType,
            actionTypeName,
            occurredAt
        );
    }
}
