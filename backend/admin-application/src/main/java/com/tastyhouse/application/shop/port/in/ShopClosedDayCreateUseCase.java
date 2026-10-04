package com.tastyhouse.application.shop.port.in;

public interface ShopClosedDayCreateUseCase {

    Long createClosedDay(ShopClosedDayManagementCreateCommand command);
}
