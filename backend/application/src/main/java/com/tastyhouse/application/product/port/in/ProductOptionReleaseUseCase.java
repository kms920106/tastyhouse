package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.product.port.out.ProductAvailabilityChangeView;
import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public interface ProductOptionReleaseUseCase {

    ProductAvailabilityChangeView releaseOptions(ProductOptionReleaseCommand command);
}
