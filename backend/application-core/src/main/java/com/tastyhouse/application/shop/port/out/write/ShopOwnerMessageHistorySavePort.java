package com.tastyhouse.application.shop.port.out.write;

import com.tastyhouse.domain.shop.model.ShopOwnerMessageHistory;

public interface ShopOwnerMessageHistorySavePort {

    void saveOwnerMessage(ShopOwnerMessageHistory ownerMessageHistory);
}
