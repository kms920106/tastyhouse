package com.tastyhouse.adminapi.shop.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "배달지역 조정 신청 상세")
public record ShopDeliveryAreaAdjustmentDetailResponse(
    @Schema(description = "신청 ID", example = "1")
    Long id,

    @Schema(description = "신청 가게 ID", example = "1")
    Long shopId,

    @Schema(description = "신청 가게명", example = "맛있는집 역삼점")
    String shopName,

    @Schema(description = "상대 가맹점 상호명", example = "맛있는집 강남점")
    String counterpartShopName,

    @Schema(description = "상대 가맹점 사업자등록번호(하이픈 제외 10자리)", example = "1234567890")
    String counterpartBusinessNumber,

    @Schema(description = "가맹본부명", example = "맛있는집 본사")
    String franchiseName,

    @Schema(description = "배달지역 중첩 사유", example = "역삼1동 전역이 중첩되어 주문이 분산됩니다.")
    String reason,

    @Schema(description = "정보제공 동의서 표시용 URL. 없으면 null", example = "https://storage.example.com/2026/08/09/uuid.pdf")
    String consentFileUrl,

    @Schema(description = "처리 상태", example = "PENDING", allowableValues = {"PENDING", "IN_PROGRESS", "COMPLETED", "REJECTED"})
    String status,

    @Schema(description = "반려 사유. 반려가 아니면 null", example = "동의서가 식별되지 않습니다.")
    String rejectReason,

    @Schema(description = "접수 일시", example = "2026-08-09T10:00:00")
    LocalDateTime createdAt,

    @Schema(description = "수정 일시", example = "2026-08-09T12:00:00")
    LocalDateTime updatedAt
) {

    public static ShopDeliveryAreaAdjustmentDetailResponse from(
        Long id,
        Long shopId,
        String shopName,
        String counterpartShopName,
        String counterpartBusinessNumber,
        String franchiseName,
        String reason,
        String consentFileUrl,
        String status,
        String rejectReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ShopDeliveryAreaAdjustmentDetailResponse(
            id,
            shopId,
            shopName,
            counterpartShopName,
            counterpartBusinessNumber,
            franchiseName,
            reason,
            consentFileUrl,
            status,
            rejectReason,
            createdAt,
            updatedAt
        );
    }
}
