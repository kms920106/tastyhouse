package com.tastyhouse.application.shop.port.out.write;

import java.util.List;
import java.util.Optional;

public interface ShopMenuCollectionImageStatePort {
    ShopMenuCollectionImageState save(ShopMenuCollectionImageState image);

    Optional<ShopMenuCollectionImageState> findById(Long id);

    List<ShopMenuCollectionImageState> findAllByShopId(Long shopId);

    void delete(ShopMenuCollectionImageState image);
}
