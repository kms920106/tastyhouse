package com.tastyhouse.application.product.port.out;

import java.util.Optional;

public interface ProductBbqSyncQueryPort {

    Optional<ProductBbqSyncTargetResult> findFirstBbqSyncTarget();
}
