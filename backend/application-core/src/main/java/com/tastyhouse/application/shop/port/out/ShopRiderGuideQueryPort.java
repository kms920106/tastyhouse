package com.tastyhouse.application.shop.port.out;

import java.util.Optional;

public interface ShopRiderGuideQueryPort {

    Optional<ShopRiderGuideResult> findRiderGuide(Long shopId);
}
