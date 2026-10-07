package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopBreakTimeResult;

public interface ShopBreakTimeManagementQueryUseCase {

    List<ShopBreakTimeResult> getBreakTimes(Long id);
}
