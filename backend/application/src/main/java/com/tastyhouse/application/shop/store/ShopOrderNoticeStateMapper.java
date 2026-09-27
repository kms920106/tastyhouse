package com.tastyhouse.application.shop.store;

import com.tastyhouse.application.shop.port.out.write.ShopOrderNoticeState;
import com.tastyhouse.domain.shop.model.ShopOrderNotice;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shop.vo.ShopOrderNoticeId;

final class ShopOrderNoticeStateMapper {
    private ShopOrderNoticeStateMapper() {
    }

    static ShopOrderNotice toDomain(ShopOrderNoticeState state) {
        return ShopOrderNotice.reconstitute(
            state.id() == null ? null : ShopOrderNoticeId.of(state.id()),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.content(),
            state.hidden(),
            state.hiddenReason(),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static ShopOrderNoticeState toState(ShopOrderNotice shopOrderNotice) {
        return new ShopOrderNoticeState(
            shopOrderNotice.getId() == null ? null : shopOrderNotice.getId().value(),
            shopOrderNotice.getShopId() == null ? null : shopOrderNotice.getShopId().value(),
            shopOrderNotice.getContent(),
            shopOrderNotice.isHidden(),
            shopOrderNotice.getHiddenReason(),
            shopOrderNotice.getCreatedAt(),
            shopOrderNotice.getUpdatedAt()
        );
    }
}
