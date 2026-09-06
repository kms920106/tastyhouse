package com.tastyhouse.domain.shop.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopSuspension;

public interface ShopSuspensionRepository {
    ShopSuspension save(ShopSuspension shopSuspension);

    List<ShopSuspension> findByShopId(Long shopId);

    Optional<ShopSuspension> findById(Long id);
}
