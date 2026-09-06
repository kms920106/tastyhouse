package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public interface ProductVegetarianCommandUseCase {

    Long requestVegetarian(ProductVegetarianRequestCommand command);

    void clearVegetarian(ProductVegetarianClearCommand command);
}
