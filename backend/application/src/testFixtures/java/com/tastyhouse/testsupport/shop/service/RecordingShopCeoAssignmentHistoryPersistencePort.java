package com.tastyhouse.testsupport.shop.service;

import java.util.ArrayList;
import java.util.List;

import com.tastyhouse.domain.shop.model.ShopCeoAssignmentHistory;
import com.tastyhouse.application.shop.port.out.write.ShopCeoAssignmentHistoryPersistencePort;

public class RecordingShopCeoAssignmentHistoryPersistencePort implements ShopCeoAssignmentHistoryPersistencePort {

    private final List<ShopCeoAssignmentHistory> saved = new ArrayList<>();

    @Override
    public ShopCeoAssignmentHistory save(ShopCeoAssignmentHistory shopCeoAssignmentHistory) {
        saved.add(shopCeoAssignmentHistory);
        return shopCeoAssignmentHistory;
    }

    public List<ShopCeoAssignmentHistory> saved() {
        return List.copyOf(saved);
    }
}
