package com.tastyhouse.testsupport.shop.service;

import java.util.ArrayList;
import java.util.List;

import com.tastyhouse.domain.shop.model.ShopChangeHistory;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.application.shop.port.out.write.ShopChangeHistorySavePort;

public class RecordingShopChangeHistorySavePort implements ShopChangeHistorySavePort {

    private final List<ShopChangeHistory> saved = new ArrayList<>();

    @Override
    public ShopChangeHistory save(ShopChangeHistory shopChangeHistory) {
        saved.add(shopChangeHistory);
        return shopChangeHistory;
    }

    public List<ShopChangeHistory> saved() {
        return List.copyOf(saved);
    }

    public List<ShopChangeHistory> savedOf(ShopChangeType changeType) {
        return saved.stream()
            .filter(history -> history.getChangeType() == changeType)
            .toList();
    }
}
