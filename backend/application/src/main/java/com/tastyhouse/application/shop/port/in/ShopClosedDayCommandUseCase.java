package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public interface ShopClosedDayCommandUseCase {

    void updateHolidayClosure(ShopHolidayClosureUpdateCommand command);

    Long createClosedDay(ShopClosedDayOwnerCreateCommand command);

    void deleteClosedDay(ShopClosedDayOwnerDeleteCommand command);

    Long createTemporaryClosure(ShopTemporaryClosureCreateCommand command);

    void deleteTemporaryClosure(ShopTemporaryClosureDeleteCommand command);
}
