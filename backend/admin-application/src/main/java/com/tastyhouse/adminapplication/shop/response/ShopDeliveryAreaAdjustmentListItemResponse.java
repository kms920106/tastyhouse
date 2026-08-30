package com.tastyhouse.adminapplication.shop.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 배달지역 조정 신청 목록 항목(검수 화면).
 *
 * <p>중첩 사유·동의서는 담지 않는다 — 상세 조회에서 본다.
 */
@Schema(description = "배달지역 조정 신청 목록 항목")
public record ShopDeliveryAreaAdjustmentListItemResponse(
    @Schema(description = "신청 ID", example = "1")
    Long id,

    @Schema(description = "신청 가게 ID", example = "1")
    Long shopId,

    @Schema(description = "신청 가게명", example = "맛있는집 역삼점")
    String shopName,

    @Schema(description = "상대 가맹점 상호명", example = "맛있는집 강남점")
    String counterpartShopName,

    @Schema(description = "가맹본부명", example = "맛있는집 본사")
    String franchiseName,

    @Schema(description = "처리 상태", example = "PENDING", allowableValues = {"PENDING", "IN_PROGRESS", "COMPLETED", "REJECTED"})
    String status,

    @Schema(description = "접수 일시", example = "2026-08-09T10:00:00")
    LocalDateTime createdAt
) {

    public static ShopDeliveryAreaAdjustmentListItemResponse from(
        Long id,
        Long shopId,
        String shopName,
        String counterpartShopName,
        String franchiseName,
        String status,
        LocalDateTime createdAt
    ) {
        return new ShopDeliveryAreaAdjustmentListItemResponse(
            id,
            shopId,
            shopName,
            counterpartShopName,
            franchiseName,
            status,
            createdAt
        );
    }
}
