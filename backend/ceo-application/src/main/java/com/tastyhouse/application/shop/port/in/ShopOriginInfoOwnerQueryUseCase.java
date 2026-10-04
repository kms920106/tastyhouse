package com.tastyhouse.application.shop.port.in;

import java.util.Optional;

import com.tastyhouse.application.shop.port.out.ShopOriginInfoResult;

public interface ShopOriginInfoOwnerQueryUseCase {

    Optional<ShopOriginInfoResult> getOriginInfo(Long ceoId, Long shopId);
}
