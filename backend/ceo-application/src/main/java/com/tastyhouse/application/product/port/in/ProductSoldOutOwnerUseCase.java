package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.product.port.out.ProductAvailabilityChangeView;

public interface ProductSoldOutOwnerUseCase {

    ProductAvailabilityChangeView markProductsSoldOut(ProductSoldOutOwnerCommand command);
}
