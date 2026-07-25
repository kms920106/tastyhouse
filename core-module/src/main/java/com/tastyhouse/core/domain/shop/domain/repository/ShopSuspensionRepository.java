package com.tastyhouse.core.domain.shop.domain.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.core.domain.shop.domain.model.ShopSuspension;

public interface ShopSuspensionRepository {

    ShopSuspension save(ShopSuspension shopSuspension);

    List<ShopSuspension> findByShopId(Long shopId);

    Optional<ShopSuspension> findById(Long id);
}
