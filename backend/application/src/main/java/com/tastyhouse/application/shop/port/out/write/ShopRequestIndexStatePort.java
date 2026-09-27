package com.tastyhouse.application.shop.port.out.write;

import java.util.Optional;

public interface ShopRequestIndexStatePort {
    ShopRequestIndexState save(ShopRequestIndexState shopRequestIndex);

    Optional<ShopRequestIndexState> findById(Long id);

    Optional<ShopRequestIndexState> findByRequestTypeAndSourceRequestId(String requestType, Long sourceRequestId);
}
