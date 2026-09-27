package com.tastyhouse.application.shop.port.out.write;

import java.util.List;
import java.util.Optional;

public interface ShopTemporaryClosureStatePort {
    ShopTemporaryClosureState save(ShopTemporaryClosureState shopTemporaryClosure);

    List<ShopTemporaryClosureState> findByShopId(Long shopId);

    Optional<ShopTemporaryClosureState> findById(Long id);

    void deleteById(Long id);
}
