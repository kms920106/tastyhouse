package com.tastyhouse.application.shop.store;

import com.tastyhouse.domain.shop.model.HygieneBadgeType;
import com.tastyhouse.domain.shop.model.ShopHygieneBadge;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.out.write.ShopHygieneBadgeState;

final class ShopHygieneBadgeStateMapper {
    private ShopHygieneBadgeStateMapper() {
    }

    static ShopHygieneBadge toDomain(ShopHygieneBadgeState state) {
        return ShopHygieneBadge.reconstitute(
            state.id(),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.badgeType() == null ? null : HygieneBadgeType.valueOf(state.badgeType()),
            state.certifiedDate(),
            state.lastInspectionMonth(),
            state.createdAt()
        );
    }

    static ShopHygieneBadgeState toState(ShopHygieneBadge shopHygieneBadge) {
        return new ShopHygieneBadgeState(
            shopHygieneBadge.getId(),
            shopHygieneBadge.getShopId() == null ? null : shopHygieneBadge.getShopId().value(),
            shopHygieneBadge.getBadgeType() == null ? null : shopHygieneBadge.getBadgeType().name(),
            shopHygieneBadge.getCertifiedDate(),
            shopHygieneBadge.getLastInspectionMonth(),
            shopHygieneBadge.getCreatedAt()
        );
    }
}
