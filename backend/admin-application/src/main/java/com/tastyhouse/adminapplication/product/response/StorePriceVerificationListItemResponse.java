package com.tastyhouse.adminapplication.product.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 매장 가격 인증 요청 검수 목록 항목.
 *
 * <p>{@code priceListFileUrl}을 목록에 담는다 — 가격표 이미지가 검수의 유일한 근거이므로 검수자가
 * 목록에서 곧바로 열어볼 수 있어야 한다(사장님 추천 요청이 메뉴 이미지를 목록에 담는 것과 같은 이유).
 *
 * <p>{@code itemCount}만 담고 항목 자체는 담지 않는다 — 요청 1건에 메뉴가 N건 달려 목록에 펼치면
 * 페이징이 무의미해진다. 메뉴별 앱 가격 대 신고 매장가 대조는 상세 조회의 몫이다.
 */
@Schema(description = "매장 가격 인증 요청 검수 목록 항목")
public record StorePriceVerificationListItemResponse(
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

    @Schema(description = "인증 대상 메뉴 수", example = "8")
    Long itemCount,

    @Schema(description = "요청 접수 시각", example = "2026-08-20T14:30:00")
    LocalDateTime requestedAt,

    @Schema(description = "처리 시각(검수 착수·승인·반려·취소). 접수 직후면 null",
        example = "2026-08-21T09:10:00")
    LocalDateTime processedAt
) {

    public static StorePriceVerificationListItemResponse from(
        Long id,
        Long shopId,
        String shopName,
        String status,
        String priceListFileUrl,
        String rejectReason,
        Long itemCount,
        LocalDateTime requestedAt,
        LocalDateTime processedAt
    ) {
        return new StorePriceVerificationListItemResponse(
            id,
            shopId,
            shopName,
            status,
            priceListFileUrl,
            rejectReason,
            itemCount,
            requestedAt,
            processedAt
        );
    }
}
