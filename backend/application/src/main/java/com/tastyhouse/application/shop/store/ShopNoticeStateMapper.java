package com.tastyhouse.application.shop.store;

import com.tastyhouse.domain.shop.model.ShopNotice;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.out.write.ShopNoticeState;

final class ShopNoticeStateMapper {
    private ShopNoticeStateMapper() {
    }

    static ShopNotice toDomain(ShopNoticeState state) {
        return ShopNotice.reconstitute(
            state.id(),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.content(),
            state.exposed(),
            state.hidden(),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static ShopNoticeState toState(ShopNotice shopNotice) {
        return new ShopNoticeState(
            shopNotice.getId(),
            shopNotice.getShopId() == null ? null : shopNotice.getShopId().value(),
            shopNotice.getContent(),
            shopNotice.isExposed(),
            shopNotice.isHidden(),
            shopNotice.getCreatedAt(),
            shopNotice.getUpdatedAt()
        );
    }
}
