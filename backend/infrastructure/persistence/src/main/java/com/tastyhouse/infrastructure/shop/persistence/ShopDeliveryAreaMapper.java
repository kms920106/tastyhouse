package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shop.model.DeliveryAreaSource;
import com.tastyhouse.domain.shop.model.ShopDeliveryArea;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ShopDeliveryAreaMapper {
    private ShopDeliveryAreaMapper() {
    }

    static ShopDeliveryArea toDomain(ShopDeliveryAreaJpaEntity entity) {
        return ShopDeliveryArea.reconstitute(
            entity.getId(),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getAdminDongId() == null ? null : AdminDongId.of(entity.getAdminDongId()),
            entity.getSource() == null ? DeliveryAreaSource.MANUAL : DeliveryAreaSource.valueOf(entity.getSource())
        );
    }

    static ShopDeliveryAreaJpaEntity toEntity(ShopDeliveryArea shopDeliveryArea) {
        return ShopDeliveryAreaJpaEntity.create(
            shopDeliveryArea.getShopId() == null ? null : shopDeliveryArea.getShopId().value(),
            shopDeliveryArea.getAdminDongId() == null ? null : shopDeliveryArea.getAdminDongId().value(),
            shopDeliveryArea.getSource() == null ? null : shopDeliveryArea.getSource().name()
        );
    }
}
