package com.tastyhouse.application.shop.port.out.write;

import java.util.Optional;

public interface ShopHygieneBadgeStatePort {
    Optional<ShopHygieneBadgeState> findById(Long id);

    ShopHygieneBadgeState save(ShopHygieneBadgeState shopHygieneBadge);

    void deleteById(Long id);
}
