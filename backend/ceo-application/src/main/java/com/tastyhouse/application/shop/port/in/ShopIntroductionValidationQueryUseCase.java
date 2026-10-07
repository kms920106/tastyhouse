package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shop.port.out.ShopIntroductionValidationResult;

public interface ShopIntroductionValidationQueryUseCase {

    ShopIntroductionValidationResult validateIntroduction(Long ceoId, Long shopId, String message);
}
