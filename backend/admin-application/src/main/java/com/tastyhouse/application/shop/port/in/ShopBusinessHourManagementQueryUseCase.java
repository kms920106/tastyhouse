package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopBusinessHourResult;

public interface ShopBusinessHourManagementQueryUseCase {

    List<ShopBusinessHourResult> getBusinessHours(Long id);
}
