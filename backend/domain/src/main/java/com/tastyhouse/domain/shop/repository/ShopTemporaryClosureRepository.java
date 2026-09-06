package com.tastyhouse.domain.shop.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopTemporaryClosure;

public interface ShopTemporaryClosureRepository {
    ShopTemporaryClosure save(ShopTemporaryClosure shopTemporaryClosure);

    List<ShopTemporaryClosure> findByShopId(Long shopId);

    Optional<ShopTemporaryClosure> findById(Long id);

    void deleteById(Long id);
}
