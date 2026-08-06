package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 지역별 추가 배달팁 순수 도메인 모델 (행정동 하나에 금액 하나).
 *
 * <p>선택 가능한 행정동은 <b>가게의 배달가능지역으로 설정된 것</b>으로 제한되는데, 이는 다른 애그리거트
 * 컬렉션({@code SHOP_DELIVERY_AREA})을 읽어야 판정할 수 있으므로 {@code ShopDeliveryTipService}가
 * 담당한다 — 여기서는 행 하나만 보고 판정할 수 있는 금액 범위만 강제한다.
 */
public class ShopDeliveryTipRegion {

    private final Long id;
    private final ShopId shopId;
    private final AdminDongId adminDongId;
    private int tipAmount;

    private ShopDeliveryTipRegion(Long id, ShopId shopId, AdminDongId adminDongId, int tipAmount) {
        this.id = id;
        this.shopId = shopId;
        this.adminDongId = adminDongId;
        this.tipAmount = tipAmount;
    }

    /** 신규 지역별 배달팁을 생성한다. 금액은 {@code 0 ~ 10,000원}이어야 한다. */
    public static ShopDeliveryTipRegion of(ShopId shopId, AdminDongId adminDongId, int tipAmount) {
        validateTipAmount(tipAmount);

        return new ShopDeliveryTipRegion(null, shopId, adminDongId, tipAmount);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며 검증하지 않는다.
     */
    public static ShopDeliveryTipRegion reconstitute(Long id, ShopId shopId, AdminDongId adminDongId, int tipAmount) {
        return new ShopDeliveryTipRegion(id, shopId, adminDongId, tipAmount);
    }

    /** 금액을 변경한다 — 생성과 같은 범위 검증을 강제한다. */
    public void changeTipAmount(int tipAmount) {
        validateTipAmount(tipAmount);

        this.tipAmount = tipAmount;
    }

    /** 이 행이 주어진 행정동을 가리키는지 판정한다(배달 목적지 행정동과의 매칭). */
    public boolean matches(AdminDongId adminDongId) {
        return this.adminDongId != null && this.adminDongId.equals(adminDongId);
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

    public AdminDongId getAdminDongId() {
        return this.adminDongId;
    }

    public int getTipAmount() {
        return this.tipAmount;
    }
}
