package com.tastyhouse.application.shop.port.out.write;

import com.tastyhouse.domain.shop.model.ShopChangeHistory;

public interface ShopChangeHistorySavePort {

    ShopChangeHistory save(ShopChangeHistory shopChangeHistory);
}
