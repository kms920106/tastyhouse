package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 거리별 추가 배달팁의 할증 단위.
 *
 * <p>각 상수가 단위 거리와 <b>자기 단위에서 허용되는 금액 범위</b>를 함께 갖고
 * {@link #validateAmount(int)}를 스스로 수행한다 — 이 매핑이 서비스의 switch 문으로 흩어져 있으면
 * 단위를 추가할 때 두 곳을 함께 고쳐야 하고, 한쪽을 빠뜨리면 범위 검증이 조용히 누락된다.
 *
 * <p><b>상수 이름 자체가 DB 저장값이다</b>({@code EnumType.STRING}) — 이름을 바꾸지 않는다.
 */
public enum DeliveryTipDistanceUnit {

    /** 100m당 할증 — 100~300원. */
    PER_100M(100, 100, 300),

    /** 500m당 할증 — 100~1,500원. */
    PER_500M(500, 100, 1500);

    private final int unitMeters;
    private final int minAmount;
    private final int maxAmount;

    DeliveryTipDistanceUnit(int unitMeters, int minAmount, int maxAmount) {
        this.unitMeters = unitMeters;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
    }

    public int getUnitMeters() {
        return this.unitMeters;
    }

    public int getMinAmount() {
        return this.minAmount;
    }

    public int getMaxAmount() {
        return this.maxAmount;
    }

    /**
     * 이 단위에서 허용되는 할증 금액인지 검증한다. 범위를 벗어나면
     * {@link ErrorCode#SHOP_DELIVERY_TIP_DISTANCE_SURCHARGE_OUT_OF_RANGE}로 거절한다.
     */
    public void validateAmount(int amount) {
        if (amount < minAmount || amount > maxAmount) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_TIP_DISTANCE_SURCHARGE_OUT_OF_RANGE,
                ErrorCode.SHOP_DELIVERY_TIP_DISTANCE_SURCHARGE_OUT_OF_RANGE.getDefaultMessage()
                    + " " + name() + " 허용 범위: " + minAmount + "~" + maxAmount + "원, 입력: " + amount + "원");
        }
    }

    public static DeliveryTipDistanceUnit from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.DELIVERY_TIP_DISTANCE_UNIT_UNKNOWN,
                ErrorCode.DELIVERY_TIP_DISTANCE_UNIT_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
