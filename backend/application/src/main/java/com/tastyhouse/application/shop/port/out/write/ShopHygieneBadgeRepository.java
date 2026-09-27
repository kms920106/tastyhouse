package com.tastyhouse.application.shop.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopHygieneBadge;

public interface ShopHygieneBadgeRepository {
    Optional<ShopHygieneBadge> findById(Long id);

    ShopHygieneBadge save(ShopHygieneBadge shopHygieneBadge);

    void deleteById(Long id);
}
