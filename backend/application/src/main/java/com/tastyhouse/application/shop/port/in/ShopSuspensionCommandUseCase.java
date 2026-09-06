package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

@CeoApp
public interface ShopSuspensionCommandUseCase {

    List<Long> createSuspension(ShopSuspensionCreateCommand command);

    void releaseSuspension(ShopSuspensionReleaseCommand command);

    List<Long> createSuspensionsBulk(ShopSuspensionBulkCreateCommand command);
}
