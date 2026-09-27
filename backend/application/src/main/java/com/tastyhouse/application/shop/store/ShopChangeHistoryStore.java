package com.tastyhouse.application.shop.store;

import com.tastyhouse.application.shop.port.out.write.ShopChangeHistoryStatePort;
import com.tastyhouse.domain.shop.model.ShopChangeHistory;

public class ShopChangeHistoryStore implements ShopChangeHistoryRepository {
    private final ShopChangeHistoryStatePort shopChangeHistoryStatePort;

    public ShopChangeHistoryStore(ShopChangeHistoryStatePort shopChangeHistoryStatePort) {
        this.shopChangeHistoryStatePort = shopChangeHistoryStatePort;
    }

    @Override
    public ShopChangeHistory save(ShopChangeHistory shopChangeHistory) {
        return ShopChangeHistoryStateMapper.toDomain(shopChangeHistoryStatePort.save(ShopChangeHistoryStateMapper.toState(shopChangeHistory)));
    }
}
