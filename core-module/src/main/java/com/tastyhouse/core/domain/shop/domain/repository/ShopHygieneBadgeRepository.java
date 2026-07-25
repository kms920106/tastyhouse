package com.tastyhouse.core.domain.shop.domain.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.core.domain.shop.domain.model.ShopHygieneBadge;

public interface ShopHygieneBadgeRepository {

    List<ShopHygieneBadge> findByShopId(Long shopId);

    Optional<ShopHygieneBadge> findById(Long id);

    ShopHygieneBadge save(ShopHygieneBadge shopHygieneBadge);

    void deleteById(Long id);
}
