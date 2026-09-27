package com.tastyhouse.application.shop.service;

import java.util.ArrayList;
import java.util.List;

import com.tastyhouse.domain.shop.model.ShopCeoAssignmentHistory;
import com.tastyhouse.application.shop.store.ShopCeoAssignmentHistoryRepository;

class RecordingShopCeoAssignmentHistoryRepository implements ShopCeoAssignmentHistoryRepository {
    private final List<ShopCeoAssignmentHistory> saved = new ArrayList<>();

    @Override
    public ShopCeoAssignmentHistory save(ShopCeoAssignmentHistory shopCeoAssignmentHistory) {
        saved.add(shopCeoAssignmentHistory);
        return shopCeoAssignmentHistory;
    }

    List<ShopCeoAssignmentHistory> saved() {
        return List.copyOf(saved);
    }
}
