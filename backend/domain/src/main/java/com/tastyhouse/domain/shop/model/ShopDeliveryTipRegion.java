package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ShopDeliveryTipRegion {
    private final Long id;
    private final ShopId shopId;
    private final AdminDongId adminDongId;
    private final int tipAmount;

    private ShopDeliveryTipRegion(Long id, ShopId shopId, AdminDongId adminDongId, int tipAmount) {
        this.id = id;
        this.shopId = shopId;
        this.adminDongId = adminDongId;
        this.tipAmount = tipAmount;
    }

    public static ShopDeliveryTipRegion of(ShopId shopId, AdminDongId adminDongId, int tipAmount) {
        validateTipAmount(tipAmount);

        return new ShopDeliveryTipRegion(null, shopId, adminDongId, tipAmount);
    }

    public static ShopDeliveryTipRegion reconstitute(Long id, ShopId shopId, AdminDongId adminDongId, int tipAmount) {
        return new ShopDeliveryTipRegion(id, shopId, adminDongId, tipAmount);
    }

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
