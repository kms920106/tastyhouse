package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.product.port.out.ProductAvailabilityChangeView;
import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public interface ProductOptionHideUseCase {

    ProductAvailabilityChangeView hideOptions(ProductOptionHideCommand command);
}
