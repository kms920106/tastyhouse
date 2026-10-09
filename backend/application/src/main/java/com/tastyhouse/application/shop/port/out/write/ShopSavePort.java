package com.tastyhouse.application.shop.port.out.write;

import com.tastyhouse.domain.shop.model.Shop;

public interface ShopSavePort {

    Shop save(Shop shop);
}
