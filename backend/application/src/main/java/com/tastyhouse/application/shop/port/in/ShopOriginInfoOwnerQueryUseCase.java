package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.Optional;

import com.tastyhouse.application.shop.port.out.ShopOriginInfoResult;

@CeoApp
public interface ShopOriginInfoOwnerQueryUseCase {

    Optional<ShopOriginInfoResult> getOriginInfo(Long ceoId, Long shopId);
}
