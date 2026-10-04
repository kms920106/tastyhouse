package com.tastyhouse.application.product.port.in;

public interface ProductSortCommandUseCase {

    void reorderProductCategories(ProductCategoryReorderCommand command);

    void reorderProducts(ProductReorderCommand command);

    void relocateProducts(ProductRelocateCommand command);
}
