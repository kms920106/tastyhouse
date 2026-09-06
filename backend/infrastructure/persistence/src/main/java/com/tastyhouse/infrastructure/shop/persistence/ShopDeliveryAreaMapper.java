package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shop.model.DeliveryAreaSource;
import com.tastyhouse.domain.shop.model.ShopDeliveryArea;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ShopDeliveryAreaMapper {
    private ShopDeliveryAreaMapper() {
    }

    static ShopDeliveryArea toDomain(ShopDeliveryAreaJpaEntity entity) {
        return ShopDeliveryArea.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            IdMapping.vo(entity.getAdminDongId(), AdminDongId::of),
            entity.getSource() == null ? DeliveryAreaSource.MANUAL : entity.getSource()
        );
    }

    static ShopDeliveryAreaJpaEntity toEntity(ShopDeliveryArea domain) {
        return ShopDeliveryAreaJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            IdMapping.raw(domain.getAdminDongId(), AdminDongId::value),
            domain.getSource()
        );
    }
}
