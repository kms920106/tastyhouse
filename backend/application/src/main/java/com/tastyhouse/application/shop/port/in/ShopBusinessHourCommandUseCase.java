package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public interface ShopBusinessHourCommandUseCase {

    Long createBusinessHour(ShopBusinessHourOwnerCreateCommand command);

    void updateBusinessHour(ShopBusinessHourOwnerUpdateCommand command);

    void deleteBusinessHour(ShopBusinessHourOwnerDeleteCommand command);

    Long createBreakTime(ShopBreakTimeOwnerCreateCommand command);

    void updateBreakTime(ShopBreakTimeOwnerUpdateCommand command);

    void deleteBreakTime(ShopBreakTimeOwnerDeleteCommand command);
}
