package com.tastyhouse.domain.shop.repository;

import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopContentBoard;

public interface ShopContentBoardRepository {
    ShopContentBoard save(ShopContentBoard shopContentBoard);

    Optional<ShopContentBoard> findById(Long id);

    void deleteById(Long id);

    long countByShopId(Long shopId);
}
