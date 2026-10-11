package com.tastyhouse.application.product.port.out;

import java.util.Optional;

public interface StorePriceVerificationOwnerQueryPort {

    Optional<StorePriceVerificationOwnerLatestResult> findLatestByShopId(Long shopId);
}
