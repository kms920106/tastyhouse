package com.tastyhouse.core.domain.shop.domain.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.core.domain.shop.domain.model.ShopTemporaryClosure;

public interface ShopTemporaryClosureRepository {

    ShopTemporaryClosure save(ShopTemporaryClosure shopTemporaryClosure);

    List<ShopTemporaryClosure> findByShopId(Long shopId);

    Optional<ShopTemporaryClosure> findById(Long id);

    void deleteById(Long id);
}
