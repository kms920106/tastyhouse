package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopBreakTimeResult;
import com.tastyhouse.application.shop.port.out.ShopBusinessHourResult;

public interface ShopBusinessHourQueryUseCase {

    List<ShopBusinessHourResult> getBusinessHours(Long ceoId, Long shopId);

    List<ShopBreakTimeResult> getBreakTimes(Long ceoId, Long shopId);
}
