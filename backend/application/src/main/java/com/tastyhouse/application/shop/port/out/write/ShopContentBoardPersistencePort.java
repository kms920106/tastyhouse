package com.tastyhouse.application.shop.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopContentBoard;

public interface ShopContentBoardPersistencePort {
    ShopContentBoard save(ShopContentBoard shopContentBoard);

    Optional<ShopContentBoard> findById(Long id);

    void deleteById(Long id);

    long countByShopId(Long shopId);
}
