package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.application.shop.port.out.write.ShopPhoneNumberState;

final class ShopPhoneNumberMapper {
    private ShopPhoneNumberMapper() {
    }

    static ShopPhoneNumberState toState(ShopPhoneNumberJpaEntity entity) {
        return new ShopPhoneNumberState(
            entity.getId(),
            entity.getShopId(),
            entity.getPhoneNumber(),
            entity.isPrimary(),
            entity.isVirtual(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ShopPhoneNumberJpaEntity toEntity(ShopPhoneNumberState state) {
        return ShopPhoneNumberJpaEntity.create(
            state.shopId(),
            state.phoneNumber(),
            state.primary(),
            state.virtual()
        );
    }

    static void applyChanges(ShopPhoneNumberJpaEntity entity, ShopPhoneNumberState state) {
        entity.applyChanges(state.primary());
    }
}
