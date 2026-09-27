package com.tastyhouse.application.shop.store;

import com.tastyhouse.domain.shop.model.ShopChangeHistory;

public interface ShopChangeHistoryRepository {
    ShopChangeHistory save(ShopChangeHistory shopChangeHistory);
}
