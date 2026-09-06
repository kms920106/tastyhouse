package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import com.tastyhouse.application.shop.port.out.ShopIntroductionValidationResult;

@CeoApp
public interface ShopIntroductionQueryUseCase {

    String getIntroduction(Long ceoId, Long shopId);

    ShopIntroductionValidationResult validateIntroduction(Long ceoId, Long shopId, String message);
}
