package com.tastyhouse.application.shop.store;

import com.tastyhouse.domain.shop.model.ShopPhoneNumber;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.out.write.ShopPhoneNumberState;

final class ShopPhoneNumberStateMapper {
    private ShopPhoneNumberStateMapper() {
    }

    static ShopPhoneNumber toDomain(ShopPhoneNumberState state) {
        return ShopPhoneNumber.reconstitute(
            state.id(),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.phoneNumber(),
            state.primary(),
            state.virtual(),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static ShopPhoneNumberState toState(ShopPhoneNumber shopPhoneNumber) {
        return new ShopPhoneNumberState(
            shopPhoneNumber.getId(),
            shopPhoneNumber.getShopId() == null ? null : shopPhoneNumber.getShopId().value(),
            shopPhoneNumber.getPhoneNumber(),
            shopPhoneNumber.isPrimary(),
            shopPhoneNumber.isVirtual(),
            shopPhoneNumber.getCreatedAt(),
            shopPhoneNumber.getUpdatedAt()
        );
    }
}
