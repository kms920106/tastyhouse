package com.tastyhouse.application.shop.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopRequestIndex;
import com.tastyhouse.domain.shop.model.ShopRequestType;

public interface ShopRequestIndexLoadPort {

    Optional<ShopRequestIndex> findById(Long id);

    Optional<ShopRequestIndex> findByRequestTypeAndSourceRequestId(ShopRequestType requestType, Long sourceRequestId);
}
