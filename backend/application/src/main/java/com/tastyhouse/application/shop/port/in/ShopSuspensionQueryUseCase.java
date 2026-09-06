package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopSuspensionResult;

@CeoApp
public interface ShopSuspensionQueryUseCase {

    List<ShopSuspensionResult> getSuspensions(Long ceoId, Long shopId);
}
