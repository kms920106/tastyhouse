package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shop.port.out.ShopVisitGuideValidationResult;

public interface ShopRiderVisitGuideOwnerValidationQueryUseCase {

    ShopVisitGuideValidationResult validateVisitGuide(Long ceoId, Long shopId, String visitGuide);
}
