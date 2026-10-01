package com.tastyhouse.application.shop.service;

import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeHistory;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shared.marker.SharedApp;
import com.tastyhouse.application.shop.port.out.write.ShopChangeHistoryPersistencePort;

@SharedApp
public class ShopChangeHistoryRecorder {

    private final ShopChangeHistoryPersistencePort shopChangeHistoryPersistencePort;

    public ShopChangeHistoryRecorder(ShopChangeHistoryPersistencePort shopChangeHistoryPersistencePort) {
        this.shopChangeHistoryPersistencePort = shopChangeHistoryPersistencePort;
    }

    public void record(
        ShopId shopId,
        ShopChangeType changeType,
        ShopChangeActionType actionType,
        ShopChangeActor actor,
        String previousValue,
        String newValue
    ) {
        ShopChangeHistory history = ShopChangeHistory.of(
            shopId,
            changeType,
            actionType,
            actor,
            previousValue,
            newValue
        );
        shopChangeHistoryPersistencePort.save(history);
    }
}
