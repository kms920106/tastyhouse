package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;

/**
 * 점주 가게 휴무(공휴일 토글·정기 휴무·임시 휴무) 쓰기 인바운드 포트.
 */
@CeoApp
public interface ShopClosedDayCommandUseCase {

    void updateHolidayClosure(ShopHolidayClosureUpdateCommand command);

    Long createClosedDay(ShopClosedDayOwnerCreateCommand command);

    void deleteClosedDay(ShopClosedDayOwnerDeleteCommand command);

    Long createTemporaryClosure(ShopTemporaryClosureCreateCommand command);

    void deleteTemporaryClosure(ShopTemporaryClosureDeleteCommand command);
}
