package com.tastyhouse.domain.product.service;

import com.tastyhouse.domain.product.model.ProductHiddenReason;

public record ProductExposureResult(boolean exposed, ProductHiddenReason hiddenReason) {
    public static ProductExposureResult ofExposed() {
        return new ProductExposureResult(true, null);
    }

    public static ProductExposureResult ofHidden(ProductHiddenReason reason) {
        return new ProductExposureResult(false, reason);
    }
}
