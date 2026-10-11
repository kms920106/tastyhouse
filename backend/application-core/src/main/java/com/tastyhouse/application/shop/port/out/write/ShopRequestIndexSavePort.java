package com.tastyhouse.application.shop.port.out.write;

import com.tastyhouse.domain.shop.model.ShopRequestIndex;

public interface ShopRequestIndexSavePort {

    ShopRequestIndex save(ShopRequestIndex shopRequestIndex);
}
