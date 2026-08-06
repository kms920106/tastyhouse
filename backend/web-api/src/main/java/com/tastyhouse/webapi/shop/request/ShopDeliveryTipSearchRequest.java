package com.tastyhouse.webapi.shop.request;

import java.util.Locale;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 배달팁 조회·재견적 요청.
 *
 * <p>세 값이 모두 선택이다 — 배달팁 팝업은 주소를 아직 고르지 않은 상태에서도 열려야 하고, 그때는
 * 확정 금액 대신 하한~상한 범위를 보여준다({@code ShopDeliveryTipResponse#deliveryTip}이 {@code null}).
 * {@code deliveryAddressId}와 {@code orderAmount}가 <b>둘 다</b> 있어야 확정 계산이 가능하다.
 */
@Schema(description = "가게 배달팁 조회 요청")
public record ShopDeliveryTipSearchRequest(
    @Schema(description = "배달 주소 ID. 주문금액과 함께 주면 확정 배달팁을 계산합니다. 없으면 범위만 반환합니다.", example = "12")
    Long deliveryAddressId,

    @Schema(description = "주문금액(원). 상품 할인 후·쿠폰/포인트 차감 전 금액이며 구간별 배달팁 판정 기준입니다.", example = "15000")
    Integer orderAmount,

    @Schema(description = "주문 방법(TABLE, RESERVATION, DELIVERY, TAKEOUT). 미지정 시 DELIVERY입니다.", example = "DELIVERY")
    String orderMethod
) {

    /** 주문 방법 기본값 — 배달팁 팝업은 배달 주문 화면에서만 열리므로 미지정은 배달로 본다. */
    private static final String DEFAULT_ORDER_METHOD = "DELIVERY";

    /**
     * 미지정·공백 {@code orderMethod}를 기본값으로 정규화한다 — 소비 Service가 null 분기를 두지 않도록
     * HTTP 경계에서 끝낸다. 문자열→enum 승격은 여기서 하지 않는다(Request record는 domain-free).
     */
    public ShopDeliveryTipSearchRequest {
        orderMethod = orderMethod == null || orderMethod.isBlank()
            ? DEFAULT_ORDER_METHOD
            : orderMethod.strip().toUpperCase(Locale.ROOT);
    }
}
