package com.tastyhouse.application.shop.port.out.write;

import java.util.Optional;

public interface ShopContentBoardStatePort {
    ShopContentBoardState save(ShopContentBoardState shopContentBoard);

    Optional<ShopContentBoardState> findById(Long id);

    void deleteById(Long id);

    long countByShopId(Long shopId);
}
