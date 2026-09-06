package com.tastyhouse.domain.shop.repository;

import com.tastyhouse.domain.shop.model.ShopChangeHistory;

public interface ShopChangeHistoryRepository {
    ShopChangeHistory save(ShopChangeHistory shopChangeHistory);
}
