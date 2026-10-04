package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ShopDeliveryTipHoliday {

    private final Long id;
    private final ShopId shopId;
    private int tipAmount;

    private ShopDeliveryTipHoliday(Long id, ShopId shopId, int tipAmount) {
        this.id = id;
        this.shopId = shopId;
        this.tipAmount = tipAmount;
    }

    public static ShopDeliveryTipHoliday of(ShopId shopId, int tipAmount) {
        validateTipAmount(tipAmount);

        return new ShopDeliveryTipHoliday(null, shopId, tipAmount);
    }

    public static ShopDeliveryTipHoliday reconstitute(Long id, ShopId shopId, int tipAmount) {
        return new ShopDeliveryTipHoliday(id, shopId, tipAmount);
    }

    public void changeTipAmount(int tipAmount) {
        validateTipAmount(tipAmount);

        this.tipAmount = tipAmount;
    }

    private static void validateTipAmount(int tipAmount) {
        if (tipAmount < 0 || tipAmount > DeliveryTipPolicy.EXTRA_TIP_UPPER_BOUND) {
            throw new DomainException(DomainErrorCode.SHOP_DELIVERY_TIP_EXTRA_AMOUNT_OUT_OF_RANGE,
                DomainErrorCode.SHOP_DELIVERY_TIP_EXTRA_AMOUNT_OUT_OF_RANGE.getDefaultMessage() + " 입력: " + tipAmount + "원");
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
