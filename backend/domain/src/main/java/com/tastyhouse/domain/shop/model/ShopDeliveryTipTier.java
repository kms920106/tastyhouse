package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ShopDeliveryTipTier {
    private final Long id;
    private final ShopId shopId;
    private final int tierOrder;
    private final int minOrderAmount;
    private final int tipAmount;

    private ShopDeliveryTipTier(Long id, ShopId shopId, int tierOrder, int minOrderAmount, int tipAmount) {
        this.id = id;
        this.shopId = shopId;
        this.tierOrder = tierOrder;
        this.minOrderAmount = minOrderAmount;
        this.tipAmount = tipAmount;
    }

    public static ShopDeliveryTipTier of(ShopId shopId, int tierOrder, int minOrderAmount, int tipAmount) {
        validateTipAmount(tipAmount);
        validateMinOrderAmount(minOrderAmount);
        validateTierOrder(tierOrder);

        return new ShopDeliveryTipTier(null, shopId, tierOrder, minOrderAmount, tipAmount);
    }

    public static ShopDeliveryTipTier reconstitute(Long id, ShopId shopId, int tierOrder, int minOrderAmount, int tipAmount) {
        return new ShopDeliveryTipTier(id, shopId, tierOrder, minOrderAmount, tipAmount);
    }

    public boolean covers(int orderAmount) {
        return orderAmount >= this.minOrderAmount;
    }

    private static void validateTipAmount(int tipAmount) {
        if (tipAmount < 0 || tipAmount >= DeliveryTipPolicy.TIER_TIP_UPPER_BOUND_EXCLUSIVE) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_TIP_AMOUNT_OUT_OF_RANGE,
                ErrorCode.SHOP_DELIVERY_TIP_AMOUNT_OUT_OF_RANGE.getDefaultMessage() + " 입력: " + tipAmount + "원");
        }
    }

    private static void validateMinOrderAmount(int minOrderAmount) {
        if (minOrderAmount < 0) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_TIP_AMOUNT_OUT_OF_RANGE,
                "구간 하한 주문금액은 0원 이상이어야 합니다. 입력: " + minOrderAmount + "원");
        }
    }

    private static void validateTierOrder(int tierOrder) {
        if (tierOrder < 0 || tierOrder >= DeliveryTipPolicy.TIER_MAX_COUNT) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_TIP_TIER_LIMIT_EXCEEDED,
                ErrorCode.SHOP_DELIVERY_TIP_TIER_LIMIT_EXCEEDED.getDefaultMessage() + " 입력 순서: " + tierOrder);
        }
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public int getTierOrder() {
        return this.tierOrder;
    }

    public int getMinOrderAmount() {
        return this.minOrderAmount;
    }

    public int getTipAmount() {
        return this.tipAmount;
    }
}
