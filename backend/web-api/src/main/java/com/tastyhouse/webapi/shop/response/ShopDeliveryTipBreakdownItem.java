package com.tastyhouse.webapi.shop.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 확정 배달팁의 항목별 근거 한 줄.
 *
 * <p>{@code label}은 <b>서버가 만든다</b> — 어느 설정이 이 금액을 만들었는지는 계산 근거라 read model에
 * 속하고, 프론트가 구간·거리·시간대 규칙을 다시 구현하면 서버와 문구가 갈린다. 다만 금액의 <b>표기
 * 포맷</b>(천 단위 콤마·"원" 단위)은 프론트가 담당하므로 {@code amount}는 포맷하지 않은 정수다.
 */
@Schema(description = "배달팁 산출 근거 항목")
public record ShopDeliveryTipBreakdownItem(
    @Schema(description = "항목 설명", example = "주문금액 15,000원 이상")
    String label,

    @Schema(description = "항목 금액(원)", example = "1000")
    int amount
) {
    public static ShopDeliveryTipBreakdownItem from(
        String label,
        int amount
    ) {
        return new ShopDeliveryTipBreakdownItem(
            label,
            amount
        );
    }
}
