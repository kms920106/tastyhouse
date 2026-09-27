package com.tastyhouse.application.shop.port.out.write;

import java.util.Optional;

public interface ShopStatePort {
    Optional<ShopState> findById(Long id);

    Optional<ShopState> findVisibleById(Long id);

    ShopState save(ShopState shop);
}
