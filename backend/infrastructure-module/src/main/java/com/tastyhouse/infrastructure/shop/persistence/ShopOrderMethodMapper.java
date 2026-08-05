package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopOrderMethod;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ShopOrderMethodMapper {

    private ShopOrderMethodMapper() {
    }

    static ShopOrderMethod toDomain(ShopOrderMethodJpaEntity entity) {
        return ShopOrderMethod.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            entity.getOrderMethod()
        );
    }

    static ShopOrderMethodJpaEntity toEntity(ShopOrderMethod domain) {
        return ShopOrderMethodJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            domain.getOrderMethod()
        );
    }
}
