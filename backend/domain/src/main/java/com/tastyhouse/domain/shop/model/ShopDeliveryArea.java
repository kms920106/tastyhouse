package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ShopDeliveryArea {
    private final Long id;
    private final ShopId shopId;
    private final AdminDongId adminDongId;
    private final DeliveryAreaSource source;

    private ShopDeliveryArea(Long id, ShopId shopId, AdminDongId adminDongId, DeliveryAreaSource source) {
        this.id = id;
        this.shopId = shopId;
        this.adminDongId = adminDongId;
        this.source = source;
    }

    public static ShopDeliveryArea of(ShopId shopId, AdminDongId adminDongId) {
        return of(shopId, adminDongId, DeliveryAreaSource.MANUAL);
    }

    public static ShopDeliveryArea of(ShopId shopId, AdminDongId adminDongId, DeliveryAreaSource source) {
        if (source == null) {
            throw new IllegalArgumentException("배달가능지역의 등록 출처는 필수입니다.");
        }
        return new ShopDeliveryArea(null, shopId, adminDongId, source);
    }

    public static ShopDeliveryArea reconstitute(Long id, ShopId shopId, AdminDongId adminDongId, DeliveryAreaSource source) {
        return new ShopDeliveryArea(id, shopId, adminDongId, source);
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

    public DeliveryAreaSource getSource() {
        return this.source;
    }
}
