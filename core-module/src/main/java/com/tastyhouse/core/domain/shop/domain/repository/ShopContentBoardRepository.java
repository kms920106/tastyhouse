package com.tastyhouse.core.domain.shop.domain.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.core.domain.shop.domain.model.ShopContentBoard;

public interface ShopContentBoardRepository {

    ShopContentBoard save(ShopContentBoard shopContentBoard);

    List<ShopContentBoard> findByShopId(Long shopId);

    Optional<ShopContentBoard> findById(Long id);

    void deleteById(Long id);

    long countByShopId(Long shopId);
}
