package com.tastyhouse.application.shop.store;

import com.tastyhouse.domain.shop.model.OriginSourceType;
import com.tastyhouse.domain.shop.model.ShopOriginInfo;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.out.write.ShopOriginInfoState;

final class ShopOriginInfoStateMapper {
    private ShopOriginInfoStateMapper() {
    }

    static ShopOriginInfo toDomain(ShopOriginInfoState state) {
        return ShopOriginInfo.reconstitute(
            state.id(),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.sourceType() == null ? null : OriginSourceType.valueOf(state.sourceType()),
            state.content(),
            state.url(),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static ShopOriginInfoState toState(ShopOriginInfo shopOriginInfo) {
        return new ShopOriginInfoState(
            shopOriginInfo.getId(),
            shopOriginInfo.getShopId() == null ? null : shopOriginInfo.getShopId().value(),
            shopOriginInfo.getSourceType() == null ? null : shopOriginInfo.getSourceType().name(),
            shopOriginInfo.getContent(),
            shopOriginInfo.getUrl(),
            shopOriginInfo.getCreatedAt(),
            shopOriginInfo.getUpdatedAt()
        );
    }
}
