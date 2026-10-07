package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopClosedDayResult;

public interface ShopClosedDayManagementQueryUseCase {

    List<ShopClosedDayResult> getClosedDays(Long id);
}
