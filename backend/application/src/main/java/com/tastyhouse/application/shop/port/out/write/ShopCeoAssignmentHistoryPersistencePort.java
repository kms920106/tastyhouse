package com.tastyhouse.application.shop.port.out.write;

import com.tastyhouse.domain.shop.model.ShopCeoAssignmentHistory;

public interface ShopCeoAssignmentHistoryPersistencePort {
    ShopCeoAssignmentHistory save(ShopCeoAssignmentHistory shopCeoAssignmentHistory);
}
