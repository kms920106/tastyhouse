package com.tastyhouse.domain.shop.service;

import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeHistory;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.repository.ShopChangeHistoryRepository;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ShopChangeHistoryRecorder {
    private final ShopChangeHistoryRepository shopChangeHistoryRepository;

    public ShopChangeHistoryRecorder(ShopChangeHistoryRepository shopChangeHistoryRepository) {
        this.shopChangeHistoryRepository = shopChangeHistoryRepository;
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
        shopChangeHistoryRepository.save(history);
    }
}
