package com.tastyhouse.application.review.store;

import java.util.Optional;

import com.tastyhouse.application.review.port.out.write.ShopReviewDisplaySettingStatePort;
import com.tastyhouse.domain.review.model.ShopReviewDisplaySetting;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ShopReviewDisplaySettingStore implements ShopReviewDisplaySettingRepository {
    private final ShopReviewDisplaySettingStatePort shopReviewDisplaySettingStatePort;

    public ShopReviewDisplaySettingStore(ShopReviewDisplaySettingStatePort shopReviewDisplaySettingStatePort) {
        this.shopReviewDisplaySettingStatePort = shopReviewDisplaySettingStatePort;
    }

    @Override
    public Optional<ShopReviewDisplaySetting> findByShopId(ShopId shopId) {
        return shopReviewDisplaySettingStatePort.findByShopId(shopId.value())
            .map(ShopReviewDisplaySettingStateMapper::toDomain);
    }

    @Override
    public ShopReviewDisplaySetting save(ShopReviewDisplaySetting shopReviewDisplaySetting) {
        return ShopReviewDisplaySettingStateMapper.toDomain(
            shopReviewDisplaySettingStatePort.save(ShopReviewDisplaySettingStateMapper.toState(shopReviewDisplaySetting)));
    }
}
