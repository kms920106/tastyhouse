package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.application.product.port.out.ProductOptionGroupLinkedProductResult;

public interface ProductOptionGroupLinkedProductListQueryUseCase {

    List<ProductOptionGroupLinkedProductResult> getLinkedProducts(Long ceoId, Long shopId, Long optionGroupId);
}
