package com.tastyhouse.adminapi.product.adapter.in.web.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.product.port.out.StorePriceVerificationItemResult;
import com.tastyhouse.application.product.port.out.StorePriceVerificationListItemResult;

@Schema(description = "매장 가격 인증 요청 상세")
public record StorePriceVerificationDetailResponse(
    @Schema(description = "인증 요청 ID", example = "12")
    Long id,

    @Schema(description = "가게 ID", example = "1")
    Long shopId,

    @Schema(description = "가게명", example = "맛있는집 강남점")
    String shopName,

    @Schema(description = "인증 요청 상태", example = "PENDING",
        allowableValues = {"PENDING", "IN_PROGRESS", "APPROVED", "REJECTED", "CANCELED"})
    String status,

    @Schema(description = "매장 가격표 이미지 URL(검수 근거)",
        example = "https://example.com/price-list.jpg")
    String priceListFileUrl,

    @Schema(description = "반려 사유. 반려가 아니면 null",
        example = "가격표 이미지의 금액이 신고된 매장가와 다릅니다.")
    String rejectReason,

    @Schema(description = "요청 접수 시각", example = "2026-08-20T14:30:00")
    LocalDateTime requestedAt,

    @Schema(description = "처리 시각(검수 착수·승인·반려·취소). 접수 직후면 null",
        example = "2026-08-21T09:10:00")
    LocalDateTime processedAt,

    @Schema(description = "인증 대상 메뉴 항목 목록(앱 가격 대 신고 매장가 대조표)")
    List<StorePriceVerificationItemResponse> items
) {
    public static StorePriceVerificationDetailResponse from(
        StorePriceVerificationListItemResult result,
        List<StorePriceVerificationItemResult> itemResults
    ) {
        List<StorePriceVerificationItemResponse> items = itemResults.stream()
            .map(StorePriceVerificationItemResponse::from)
            .toList();
        return new StorePriceVerificationDetailResponse(
            result.id(),
            result.shopId(),
            result.shopName(),
            result.status().name(),
            result.priceListFileUrl(),
            result.rejectReason(),
            result.requestedAt(),
            result.processedAt(),
            items
        );
    }
}
