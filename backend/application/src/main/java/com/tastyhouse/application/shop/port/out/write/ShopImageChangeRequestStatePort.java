package com.tastyhouse.application.shop.port.out.write;

import java.util.Optional;

public interface ShopImageChangeRequestStatePort {
    ShopImageChangeRequestState save(ShopImageChangeRequestState shopImageChangeRequest);

    Optional<ShopImageChangeRequestState> findById(Long id);

    boolean existsByShopIdAndImageTypeAndStatus(Long shopId, String imageType, String status);

    boolean existsByShopIdAndStatus(Long shopId, String status);
}
