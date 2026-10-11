package com.tastyhouse.application.shop.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopHygieneBadge;

public interface ShopHygieneBadgeLoadPort {

    Optional<ShopHygieneBadge> findById(Long id);
}
