package com.tastyhouse.application.shop.port.out.write;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopSuspension;

public interface ShopSuspensionPersistencePort {

    ShopSuspension save(ShopSuspension shopSuspension);

    List<ShopSuspension> findByShopId(Long shopId);

    Optional<ShopSuspension> findById(Long id);
}
