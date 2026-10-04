package com.tastyhouse.application.product.port.in;

public interface ProductOptionGroupCommandUseCase {

    Long createProductOptionGroup(ProductOptionGroupOwnerCreateCommand command);

    void updateProductOptionGroup(ProductOptionGroupUpdateCommand command);

    void deleteProductOptionGroup(ProductOptionGroupDeleteCommand command);
}
