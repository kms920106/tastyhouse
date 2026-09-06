package com.tastyhouse.domain.shop.repository;

import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopRequestIndex;
import com.tastyhouse.domain.shop.model.ShopRequestType;

public interface ShopRequestIndexRepository {
    ShopRequestIndex save(ShopRequestIndex shopRequestIndex);

    Optional<ShopRequestIndex> findById(Long id);

    Optional<ShopRequestIndex> findByRequestTypeAndSourceRequestId(ShopRequestType requestType, Long sourceRequestId);
}
