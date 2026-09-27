package com.tastyhouse.application.review.store;

import com.tastyhouse.domain.review.model.ReviewSortType;
import com.tastyhouse.domain.review.model.ShopReviewDisplaySetting;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.review.port.out.write.ShopReviewDisplaySettingState;

final class ShopReviewDisplaySettingStateMapper {
    private ShopReviewDisplaySettingStateMapper() {
    }

    static ShopReviewDisplaySetting toDomain(ShopReviewDisplaySettingState state) {
        return ShopReviewDisplaySetting.reconstitute(
            state.id(),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.sortType() == null ? null : ReviewSortType.valueOf(state.sortType()),
            state.updatedAt()
        );
    }

    static ShopReviewDisplaySettingState toState(ShopReviewDisplaySetting setting) {
        return new ShopReviewDisplaySettingState(
            setting.getId(),
            setting.getShopId() == null ? null : setting.getShopId().value(),
            setting.getSortType() == null ? null : setting.getSortType().name(),
            setting.getUpdatedAt()
        );
    }
}
