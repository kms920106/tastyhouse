package com.tastyhouse.webapi.order.request;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Schema(description = "주문 생성 요청")
public record OrderCreateRequest(
    @NotNull(message = "매장 ID는 필수입니다")
    @Schema(description = "매장 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId,

    @NotBlank(message = "주문 방법은 필수입니다")
    @Schema(description = "주문 방법", example = "TABLE", allowableValues = {"TABLE", "RESERVATION", "DELIVERY", "TAKEOUT"}, requiredMode = Schema.RequiredMode.REQUIRED)
    String orderMethod,

    @NotEmpty(message = "주문 상품은 필수입니다")
    @Valid
    @Schema(description = "주문 상품 목록", requiredMode = Schema.RequiredMode.REQUIRED)
    List<OrderProductRequest> orderProducts,

    @Schema(description = "사용할 회원 쿠폰 ID", example = "5")
    Long memberCouponId,

    @Schema(description = "배달 주소 ID. 주문 방법이 DELIVERY면 필수입니다. 서버는 이 주소에 저장된 좌표로만 거리를 계산합니다", example = "12")
    Long deliveryAddressId,

    @NotNull(message = "포인트 사용 금액은 필수입니다")
    @Min(value = 0, message = "포인트 사용 금액은 0 이상이어야 합니다")
    @Schema(description = "포인트 사용 금액", example = "1000", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer usePoint,

    @NotNull(message = "상품 금액은 필수입니다")
    @Min(value = 0, message = "상품 금액은 0 이상이어야 합니다")
    @Schema(description = "상품 금액 합계", example = "25000", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer totalProductAmount,

    @NotNull(message = "할인 금액은 필수입니다")
    @Min(value = 0, message = "할인 금액은 0 이상이어야 합니다")
    @Schema(description = "총 할인 금액", example = "3000", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer totalDiscountAmount,

    @NotNull(message = "상품 할인 금액은 필수입니다")
    @Min(value = 0, message = "상품 할인 금액은 0 이상이어야 합니다")
    @Schema(description = "상품 할인 금액", example = "2000", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer productDiscountAmount,

    @NotNull(message = "쿠폰 사용 금액은 필수입니다")
    @Min(value = 0, message = "쿠폰 사용 금액은 0 이상이어야 합니다")
    @Schema(description = "쿠폰 사용 금액", example = "1000", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer couponDiscountAmount,

    @NotNull(message = "배달팁은 필수입니다")
    @Min(value = 0, message = "배달팁은 0 이상이어야 합니다")
    @Schema(description = "배달팁(가산 항목). 배달 외 주문 방법은 0", example = "3000", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer deliveryTipAmount,

    @NotNull(message = "결제 금액은 필수입니다")
    @Min(value = 0, message = "결제 금액은 0 이상이어야 합니다")
    @Schema(description = "최종 결제 금액(= 상품 금액 - 총 할인 + 배달팁)", example = "24000", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer finalAmount,

    @Schema(
        description = "수령 예약 시각. 예약 가능 슬롯 조회에서 받은 slots[].startAt을 그대로 보냅니다. "
            + "미전송이면 즉시 주문입니다. DELIVERY·TAKEOUT만 지원하며, 서버가 슬롯을 재계산해 대조합니다.",
        example = "2026-08-08T18:00:00"
    )
    LocalDateTime scheduledAt
) {
}
