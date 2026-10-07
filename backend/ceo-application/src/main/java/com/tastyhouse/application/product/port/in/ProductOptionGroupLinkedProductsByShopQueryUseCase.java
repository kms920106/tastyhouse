package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.application.product.port.out.ProductOptionGroupLinkedProductsResult;

public interface ProductOptionGroupLinkedProductsByShopQueryUseCase {

    List<ProductOptionGroupLinkedProductsResult> getLinkedProductsByShop(Long ceoId, Long shopId);
}
