package com.tastyhouse.application.shop.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopContentBoard;

public interface ShopContentBoardLoadPort {

    Optional<ShopContentBoard> findById(Long id);

    long countByShopId(Long shopId);
}
