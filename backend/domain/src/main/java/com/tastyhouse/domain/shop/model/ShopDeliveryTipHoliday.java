package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 공휴일 추가 배달팁 순수 도메인 모델 (가게당 1건).
 *
 * <p>배민 가이드가 "법정 공휴일에 일괄 부과"라고 규정하므로 날짜별이 아니라 <b>가게당 단일 금액</b>이다.
 * 공휴일 판정은 {@code PublicHolidayCalendar}가 담당하고, 이 애그리거트는 금액만 들고 있다.
 *
 * <p>일요일은 이 팁의 대상이 아니다 — 캘린더가 일요일 자체를 담지 않는다는 데이터 규칙으로 처리되며
 * (상세는 {@code PublicHoliday} Javadoc), 여기에 요일 분기는 없다.
 */
public class ShopDeliveryTipHoliday {

    private final Long id;
    private final ShopId shopId;
    private int tipAmount;

    private ShopDeliveryTipHoliday(Long id, ShopId shopId, int tipAmount) {
        this.id = id;
        this.shopId = shopId;
        this.tipAmount = tipAmount;
    }

    /** 신규 공휴일 배달팁을 생성한다. 금액은 {@code 0 ~ 10,000원}이어야 한다. */
    public static ShopDeliveryTipHoliday of(ShopId shopId, int tipAmount) {
        validateTipAmount(tipAmount);

        return new ShopDeliveryTipHoliday(null, shopId, tipAmount);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며 검증하지 않는다.
     */
    public static ShopDeliveryTipHoliday reconstitute(Long id, ShopId shopId, int tipAmount) {
        return new ShopDeliveryTipHoliday(id, shopId, tipAmount);
    }

    /**
     * 금액을 변경한다 — 생성과 같은 범위 검증을 강제한다(생성만 막고 변경을 열어두면 뒷문이 된다).
     */
    public void changeTipAmount(int tipAmount) {
        validateTipAmount(tipAmount);

        this.tipAmount = tipAmount;
    }

    private static void validateTipAmount(int tipAmount) {
        if (tipAmount < 0 || tipAmount > DeliveryTipPolicy.EXTRA_TIP_UPPER_BOUND) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_TIP_EXTRA_AMOUNT_OUT_OF_RANGE,
                ErrorCode.SHOP_DELIVERY_TIP_EXTRA_AMOUNT_OUT_OF_RANGE.getDefaultMessage() + " 입력: " + tipAmount + "원");
        }
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public int getTipAmount() {
        return this.tipAmount;
    }
}
