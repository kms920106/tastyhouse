package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public interface ProductSortCommandUseCase {

    void reorderProductCategories(ProductCategoryReorderCommand command);

    void reorderProducts(ProductReorderCommand command);

    void relocateProducts(ProductRelocateCommand command);
}
