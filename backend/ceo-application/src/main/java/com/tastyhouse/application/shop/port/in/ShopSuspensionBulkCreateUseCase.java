package com.tastyhouse.application.shop.port.in;

import java.util.List;

public interface ShopSuspensionBulkCreateUseCase {

    List<Long> createSuspensionsBulk(ShopSuspensionBulkCreateCommand command);
}
