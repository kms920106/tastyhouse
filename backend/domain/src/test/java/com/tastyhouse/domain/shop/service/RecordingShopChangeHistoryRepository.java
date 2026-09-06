package com.tastyhouse.domain.shop.service;

import java.util.ArrayList;
import java.util.List;

import com.tastyhouse.domain.shop.model.ShopChangeHistory;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.repository.ShopChangeHistoryRepository;

class RecordingShopChangeHistoryRepository implements ShopChangeHistoryRepository {
    private final List<ShopChangeHistory> saved = new ArrayList<>();

    @Override
    public ShopChangeHistory save(ShopChangeHistory shopChangeHistory) {
        saved.add(shopChangeHistory);
        return shopChangeHistory;
    }

    List<ShopChangeHistory> saved() {
        return List.copyOf(saved);
    }

    List<ShopChangeHistory> savedOf(ShopChangeType changeType) {
        return saved.stream()
            .filter(history -> history.getChangeType() == changeType)
            .toList();
    }
}
