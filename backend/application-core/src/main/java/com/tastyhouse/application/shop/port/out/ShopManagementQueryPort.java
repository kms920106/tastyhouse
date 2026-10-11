package com.tastyhouse.application.shop.port.out;

import java.util.Optional;

public interface ShopManagementQueryPort {

    Optional<ShopManagementDetailResult> findManagementDetailById(Long shopId);
}
