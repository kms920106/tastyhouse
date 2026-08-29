package com.tastyhouse.ceoapi.shop.application.port.in;

/**
 * 점주 가게 휴무(공휴일 토글·정기 휴무·임시 휴무) 쓰기 인바운드 포트.
 */
public interface ShopClosedDayCommandUseCase {

    void updateHolidayClosure(ShopHolidayClosureUpdateCommand command);

    Long createClosedDay(ShopClosedDayCreateCommand command);

    void deleteClosedDay(ShopClosedDayDeleteCommand command);

    Long createTemporaryClosure(ShopTemporaryClosureCreateCommand command);

    void deleteTemporaryClosure(ShopTemporaryClosureDeleteCommand command);
}
