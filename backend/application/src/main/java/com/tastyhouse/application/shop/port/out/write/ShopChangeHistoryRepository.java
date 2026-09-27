package com.tastyhouse.application.shop.port.out.write;

import com.tastyhouse.domain.shop.model.ShopChangeHistory;

public interface ShopChangeHistoryRepository {
    ShopChangeHistory save(ShopChangeHistory shopChangeHistory);
}
