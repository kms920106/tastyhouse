package com.tastyhouse.application.shop.port.out.write;

import com.tastyhouse.domain.shop.model.ShopImageChangeRequest;

public interface ShopImageChangeRequestSavePort {

    ShopImageChangeRequest save(ShopImageChangeRequest shopImageChangeRequest);
}
