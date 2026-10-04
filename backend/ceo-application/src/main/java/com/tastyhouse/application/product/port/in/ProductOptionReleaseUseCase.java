package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.product.port.out.ProductAvailabilityChangeView;

public interface ProductOptionReleaseUseCase {

    ProductAvailabilityChangeView releaseOptions(ProductOptionReleaseCommand command);
}
