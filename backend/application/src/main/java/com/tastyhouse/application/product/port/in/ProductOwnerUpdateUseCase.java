package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public interface ProductOwnerUpdateUseCase {

    void updateProduct(ProductOwnerUpdateCommand command);
}
