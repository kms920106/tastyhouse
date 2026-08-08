package com.tastyhouse.domain.shop.service;

import com.tastyhouse.domain.shop.model.OrderUnavailableReason;
import com.tastyhouse.domain.shop.model.ShopOperatingStatus;

/**
 * {@link ShopOperatingStatusCalculator}의 판정 결과 — 상태와 그 사유를 함께 나른다.
 *
 * <p>상태만 돌려주면 화면이 "왜 준비중인지"를 보여줄 수 없고, 주문 거절 시 어느 조건에 걸렸는지 추적할
 * 수 없다({@code ShopDeliveryTipBreakdown}이 총액이 아니라 항목별 내역을 돌려주는 것과 같은 이유).
 *
 * @param status            영업중/준비중
 * @param unavailableReason 준비중인 사유. {@code status == OPEN}이면 null
 */
public record ShopOperatingStatusResult(
    ShopOperatingStatus status,
    OrderUnavailableReason unavailableReason
) {

    public static ShopOperatingStatusResult open() {
        return new ShopOperatingStatusResult(ShopOperatingStatus.OPEN, null);
    }

    public static ShopOperatingStatusResult preparing(OrderUnavailableReason reason) {
        return new ShopOperatingStatusResult(
            ShopOperatingStatus.PREPARING,
            reason
        );
    }

    public boolean isOpen() {
        return status == ShopOperatingStatus.OPEN;
    }
}
