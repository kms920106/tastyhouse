package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shop.port.out.ShopStorePriceVerificationViewResult;

public interface ShopStorePriceVerificationQueryUseCase {

    ShopStorePriceVerificationViewResult getLatestVerification(Long ceoId, Long shopId);
}
