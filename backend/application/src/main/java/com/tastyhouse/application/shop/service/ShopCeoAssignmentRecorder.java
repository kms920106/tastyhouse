package com.tastyhouse.application.shop.service;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.shop.model.ShopCeoAssignmentActionType;
import com.tastyhouse.domain.shop.model.ShopCeoAssignmentHistory;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.out.write.ShopCeoAssignmentHistoryPersistencePort;

public class ShopCeoAssignmentRecorder {
    private final ShopCeoAssignmentHistoryPersistencePort shopCeoAssignmentHistoryPersistencePort;

    public ShopCeoAssignmentRecorder(
        ShopCeoAssignmentHistoryPersistencePort shopCeoAssignmentHistoryPersistencePort
    ) {
        this.shopCeoAssignmentHistoryPersistencePort = shopCeoAssignmentHistoryPersistencePort;
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
        shopCeoAssignmentHistoryPersistencePort.save(history);
    }
}
