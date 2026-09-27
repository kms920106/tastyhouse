package com.tastyhouse.application.shop.store;

import java.util.Optional;

import com.tastyhouse.application.shop.port.out.write.ShopRiderGuideStatePort;
import com.tastyhouse.domain.shop.model.ShopRiderGuide;
import com.tastyhouse.domain.shop.model.ShopRiderGuideHistory;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ShopRiderGuideStore implements ShopRiderGuideRepository {
    private final ShopRiderGuideStatePort shopRiderGuideStatePort;

    public ShopRiderGuideStore(ShopRiderGuideStatePort shopRiderGuideStatePort) {
        this.shopRiderGuideStatePort = shopRiderGuideStatePort;
    }

    @Override
    public Optional<ShopRiderGuide> findByShopId(ShopId shopId) {
        return shopRiderGuideStatePort.findByShopId(shopId == null ? null : shopId.value()).map(ShopRiderGuideStateMapper::toDomain);
    }

    @Override
    public ShopRiderGuide save(ShopRiderGuide riderGuide) {
        return ShopRiderGuideStateMapper.toDomain(shopRiderGuideStatePort.save(ShopRiderGuideStateMapper.toState(riderGuide)));
    }

    @Override
    public ShopRiderGuideHistory saveHistory(ShopRiderGuideHistory history) {
        return ShopRiderGuideHistoryStateMapper.toDomain(shopRiderGuideStatePort.saveHistory(ShopRiderGuideHistoryStateMapper.toState(history)));
    }
}
