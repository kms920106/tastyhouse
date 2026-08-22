package com.tastyhouse.adminapi.product.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 매장 가격 인증 요청 상세 — 검수 판정에 필요한 모든 근거를 한 응답에 담는다.
 *
 * <p><b>상세가 목록과 별도로 필요한 이유</b>는 검수의 실질이 <b>메뉴별 대조</b>라는 데 있다. 검수자는
 * 가격표 이미지 한 장과 신고된 매장가 N건을 한 줄씩 맞춰 봐야 하며, 이 대조표 없이는 승인 버튼을 누를
 * 근거가 없다. 목록에 항목을 펼치면 요청 1건이 N행으로 부풀어 페이징이 깨지므로 상세로 분리했다.
 *
 * <p>{@code items}는 접수 순서를 유지한다 — 점주가 가격표에 적은 순서와 같아 대조가 쉽다.
 */
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
        Long id,
        Long shopId,
        String shopName,
        String status,
        String priceListFileUrl,
        String rejectReason,
        LocalDateTime requestedAt,
        LocalDateTime processedAt,
        List<StorePriceVerificationItemResponse> items
    ) {
        return new StorePriceVerificationDetailResponse(
            id,
            shopId,
            shopName,
            status,
            priceListFileUrl,
            rejectReason,
            requestedAt,
            processedAt,
            items
        );
    }
}
