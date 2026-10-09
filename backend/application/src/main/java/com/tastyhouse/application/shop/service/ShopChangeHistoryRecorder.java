package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeHistory;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.out.write.ShopChangeHistorySavePort;

@Service
public class ShopChangeHistoryRecorder {

    private final ShopChangeHistorySavePort shopChangeHistorySavePort;

    public ShopChangeHistoryRecorder(ShopChangeHistorySavePort shopChangeHistorySavePort) {
        this.shopChangeHistorySavePort = shopChangeHistorySavePort;
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
        shopChangeHistorySavePort.save(history);
    }
}
