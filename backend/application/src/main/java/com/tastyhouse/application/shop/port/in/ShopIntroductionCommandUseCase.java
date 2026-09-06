package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public interface ShopIntroductionCommandUseCase {

    void updateIntroduction(ShopIntroductionUpdateCommand command);
}
