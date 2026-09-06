package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import com.tastyhouse.application.shop.port.out.ShopRiderGuideResult;
import com.tastyhouse.application.shop.port.out.ShopVisitGuideValidationResult;

@CeoApp
public interface ShopRiderGuideOwnerQueryUseCase {

    ShopRiderGuideResult getRiderGuide(Long ceoId, Long shopId);

    ShopVisitGuideValidationResult validateVisitGuide(Long ceoId, Long shopId, String visitGuide);
}
