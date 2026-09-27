package com.tastyhouse.application.shop.store;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.shop.model.ShopBookmark;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.out.write.ShopBookmarkState;

final class ShopBookmarkStateMapper {
    private ShopBookmarkStateMapper() {
    }

    static ShopBookmark toDomain(ShopBookmarkState state) {
        return ShopBookmark.reconstitute(
            state.id(),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.memberId() == null ? null : MemberId.of(state.memberId())
        );
    }

    static ShopBookmarkState toState(ShopBookmark shopBookmark) {
        return new ShopBookmarkState(
            shopBookmark.getId(),
            shopBookmark.getShopId() == null ? null : shopBookmark.getShopId().value(),
            shopBookmark.getMemberId() == null ? null : shopBookmark.getMemberId().value()
        );
    }
}
