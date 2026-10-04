package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shop.port.out.ShopIntroductionValidationResult;

public interface ShopIntroductionQueryUseCase {

    String getIntroduction(Long ceoId, Long shopId);

    ShopIntroductionValidationResult validateIntroduction(Long ceoId, Long shopId, String message);
}
