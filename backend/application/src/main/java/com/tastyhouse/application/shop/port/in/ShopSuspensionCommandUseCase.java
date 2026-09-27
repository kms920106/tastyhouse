package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public interface ShopSuspensionCommandUseCase {

    List<Long> createSuspension(ShopSuspensionCreateCommand command);

    void releaseSuspension(ShopSuspensionReleaseCommand command);

    List<Long> createSuspensionsBulk(ShopSuspensionBulkCreateCommand command);
}
