package com.tastyhouse.webapi.order.adapter.in.web.request;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.webapplication.order.port.in.OrderCreateCommand;
import com.tastyhouse.webapplication.order.port.in.OrderLineCommand;

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

    @Min(value = 0, message = "일회용컵 보증금은 0 이상이어야 합니다")
    @Schema(description = "일회용컵 보증금 합계(가산 항목). 서버가 선택 옵션의 컵 개수 × 300원으로 "
        + "계산한 값과 대조하며, 다르면 ORDER_CUP_DEPOSIT_AMOUNT_MISMATCH로 거절합니다. "
        + "생략하면 0으로 봅니다 — 보증금 옵션을 고르지 않은 주문은 이 필드 없이도 그대로 통과하므로 "
        + "프론트 배포 순서와 독립입니다. 이 금액은 최소주문금액·쿠폰·포인트 산정에서 제외됩니다.",
        example = "300")
    Integer cupDepositAmount,

    @NotNull(message = "결제 금액은 필수입니다")
    @Min(value = 0, message = "결제 금액은 0 이상이어야 합니다")
    @Schema(description = "최종 결제 금액(= 상품 금액 - 총 할인 + 배달팁 + 일회용컵 보증금)",
        example = "24000", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer finalAmount,

    @Schema(
        description = "수령 예약 시각. 예약 가능 슬롯 조회에서 받은 slots[].startAt을 그대로 보냅니다. "
            + "미전송이면 즉시 주문입니다. DELIVERY·TAKEOUT만 지원하며, 서버가 슬롯을 재계산해 대조합니다.",
        example = "2026-08-08T18:00:00"
    )
    LocalDateTime scheduledAt
) {

    /**
     * 인증 주체의 {@code memberId}를 주입받아 command로 변환한다.
     *
     * <p><b>이 record의 필드 선언 순서는 {@link OrderCreateCommand}와 다르다</b> —
     * 여기서는 {@code deliveryAddressId}가 {@code usePoint}보다 먼저 선언돼 있다. 그래서 아래는
     * 반드시 이름 기반 접근자로 각 값을 짚어 넘긴다(위치 기반으로 옮기면 두 값이 조용히 뒤바뀐다).
     */
    public OrderCreateCommand toCommand(Long memberId) {
        List<OrderLineCommand> orderLineCommands = orderProducts == null ? null :
            orderProducts.stream()
                .map(OrderProductRequest::toCommand)
                .toList();
        return new OrderCreateCommand(
            memberId,
            shopId,
            orderMethod,
            orderLineCommands,
            memberCouponId,
            usePoint,
            deliveryAddressId,
            totalProductAmount,
            totalDiscountAmount,
            productDiscountAmount,
            couponDiscountAmount,
            deliveryTipAmount,
            cupDepositAmount,
            finalAmount,
            scheduledAt
        );
    }
}
