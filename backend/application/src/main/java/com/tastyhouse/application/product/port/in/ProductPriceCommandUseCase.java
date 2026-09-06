package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public interface ProductPriceCommandUseCase {

    void replacePrices(ProductPriceReplaceCommand command);
}
