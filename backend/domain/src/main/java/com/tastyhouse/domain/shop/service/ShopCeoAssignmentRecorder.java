package com.tastyhouse.domain.shop.service;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.shop.model.ShopCeoAssignmentActionType;
import com.tastyhouse.domain.shop.model.ShopCeoAssignmentHistory;
import com.tastyhouse.domain.shop.repository.ShopCeoAssignmentHistoryRepository;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ShopCeoAssignmentRecorder {
    private final ShopCeoAssignmentHistoryRepository shopCeoAssignmentHistoryRepository;

    public ShopCeoAssignmentRecorder(
        ShopCeoAssignmentHistoryRepository shopCeoAssignmentHistoryRepository
    ) {
        this.shopCeoAssignmentHistoryRepository = shopCeoAssignmentHistoryRepository;
    }

    public void recordGrant(ShopId shopId, CeoId ceoId, Long actorAdminId) {
        record(shopId, ceoId, ShopCeoAssignmentActionType.GRANT, actorAdminId);
    }

    public void recordRevoke(ShopId shopId, CeoId ceoId, Long actorAdminId) {
        record(shopId, ceoId, ShopCeoAssignmentActionType.REVOKE, actorAdminId);
    }

    private void record(
        ShopId shopId,
        CeoId ceoId,
        ShopCeoAssignmentActionType actionType,
        Long actorAdminId
    ) {
        ShopCeoAssignmentHistory history = ShopCeoAssignmentHistory.of(
            shopId,
            ceoId,
            actionType,
            actorAdminId
        );
        shopCeoAssignmentHistoryRepository.save(history);
    }
}
