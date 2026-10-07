package com.tastyhouse.application.shop.port.in;

import java.util.List;

public interface ShopSuspensionCreateUseCase {

    List<Long> createSuspension(ShopSuspensionCreateCommand command);
}
