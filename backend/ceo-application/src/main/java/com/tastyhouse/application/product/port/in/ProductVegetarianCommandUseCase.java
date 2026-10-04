package com.tastyhouse.application.product.port.in;

public interface ProductVegetarianCommandUseCase {

    Long requestVegetarian(ProductVegetarianRequestCommand command);

    void clearVegetarian(ProductVegetarianClearCommand command);
}
