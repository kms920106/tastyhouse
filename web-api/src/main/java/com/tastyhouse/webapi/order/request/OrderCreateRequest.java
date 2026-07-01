package com.tastyhouse.webapi.order.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import com.tastyhouse.core.domain.shop.domain.model.OrderMethod;

@Schema(description = "주문 생성 요청")
public record OrderCreateRequest(
    @NotNull(message = "매장 ID는 필수입니다")
    @Schema(description = "매장 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId,

    @NotNull(message = "주문 방법은 필수입니다")
    @Schema(description = "주문 방법", example = "TABLE", requiredMode = Schema.RequiredMode.REQUIRED)
    OrderMethod orderMethod,

    @NotEmpty(message = "주문 상품은 필수입니다")
    @Valid
    @Schema(description = "주문 상품 목록", requiredMode = Schema.RequiredMode.REQUIRED)
    List<OrderProductRequest> orderProducts,

    @Schema(description = "사용할 회원 쿠폰 ID", example = "5")
    Long memberCouponId,

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

    @NotNull(message = "결제 금액은 필수입니다")
    @Min(value = 0, message = "결제 금액은 0 이상이어야 합니다")
    @Schema(description = "최종 결제 금액", example = "21000", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer finalAmount
) {
}
