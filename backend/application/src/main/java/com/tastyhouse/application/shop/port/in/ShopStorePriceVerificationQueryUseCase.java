package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import com.tastyhouse.application.shop.port.out.ShopStorePriceVerificationViewResult;

@CeoApp
public interface ShopStorePriceVerificationQueryUseCase {

    ShopStorePriceVerificationViewResult getLatestVerification(Long ceoId, Long shopId);
}
