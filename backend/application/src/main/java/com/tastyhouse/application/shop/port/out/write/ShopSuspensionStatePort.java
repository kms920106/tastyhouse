package com.tastyhouse.application.shop.port.out.write;

import java.util.List;
import java.util.Optional;

public interface ShopSuspensionStatePort {
    ShopSuspensionState save(ShopSuspensionState shopSuspension);

    List<ShopSuspensionState> findByShopId(Long shopId);

    Optional<ShopSuspensionState> findById(Long id);
}
