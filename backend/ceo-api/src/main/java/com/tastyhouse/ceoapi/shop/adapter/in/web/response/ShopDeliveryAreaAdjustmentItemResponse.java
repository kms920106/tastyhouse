package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaAdjustmentListItemResult;

@Schema(description = "내 가게 배달지역 조정 신청 이력 항목")
public record ShopDeliveryAreaAdjustmentItemResponse(
    @Schema(description = "신청 ID", example = "1")
    Long id,

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
    LocalDateTime createdAt
) {

    public static ShopDeliveryAreaAdjustmentItemResponse from(ShopDeliveryAreaAdjustmentListItemResult result) {
        return new ShopDeliveryAreaAdjustmentItemResponse(
            result.id(),
            result.counterpartShopName(),
            result.counterpartBusinessNumber(),
            result.franchiseName(),
            result.reason(),
            result.consentFileUrl(),
            result.status().name(),
            result.rejectReason(),
            result.createdAt()
        );
    }
}
