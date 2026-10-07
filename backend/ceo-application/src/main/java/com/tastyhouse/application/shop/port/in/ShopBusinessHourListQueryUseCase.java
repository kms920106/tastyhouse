package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopBusinessHourResult;

public interface ShopBusinessHourListQueryUseCase {

    List<ShopBusinessHourResult> getBusinessHours(Long ceoId, Long shopId);
}
