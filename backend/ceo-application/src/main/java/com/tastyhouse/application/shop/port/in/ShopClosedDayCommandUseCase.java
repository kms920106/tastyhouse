package com.tastyhouse.application.shop.port.in;

public interface ShopClosedDayCommandUseCase {

    void updateHolidayClosure(ShopHolidayClosureUpdateCommand command);

    Long createClosedDay(ShopClosedDayOwnerCreateCommand command);

    void deleteClosedDay(ShopClosedDayOwnerDeleteCommand command);

    Long createTemporaryClosure(ShopTemporaryClosureCreateCommand command);

    void deleteTemporaryClosure(ShopTemporaryClosureDeleteCommand command);
}
