package com.tastyhouse.application.shop.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.vo.ShopId;

public interface ShopRepository {
    Optional<Shop> findById(ShopId id);

    Optional<Shop> findVisibleById(ShopId id);

    Shop save(Shop shop);
}
