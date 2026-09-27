package com.tastyhouse.application.shop.store;

import com.tastyhouse.application.shop.port.out.write.ShopCeoAssignmentHistoryStatePort;
import com.tastyhouse.domain.shop.model.ShopCeoAssignmentHistory;

public class ShopCeoAssignmentHistoryStore implements ShopCeoAssignmentHistoryRepository {
    private final ShopCeoAssignmentHistoryStatePort shopCeoAssignmentHistoryStatePort;

    public ShopCeoAssignmentHistoryStore(ShopCeoAssignmentHistoryStatePort shopCeoAssignmentHistoryStatePort) {
        this.shopCeoAssignmentHistoryStatePort = shopCeoAssignmentHistoryStatePort;
    }

    @Override
    public ShopCeoAssignmentHistory save(ShopCeoAssignmentHistory shopCeoAssignmentHistory) {
        return ShopCeoAssignmentHistoryStateMapper.toDomain(shopCeoAssignmentHistoryStatePort.save(ShopCeoAssignmentHistoryStateMapper.toState(shopCeoAssignmentHistory)));
    }
}
