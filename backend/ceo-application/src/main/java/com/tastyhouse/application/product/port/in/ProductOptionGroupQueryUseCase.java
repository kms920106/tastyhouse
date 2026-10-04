package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.application.product.port.out.ProductOptionGroupLinkedProductResult;
import com.tastyhouse.application.product.port.out.ProductOptionGroupLinkedProductsResult;
import com.tastyhouse.application.product.port.out.ProductOptionGroupViewResult;

public interface ProductOptionGroupQueryUseCase {

    List<ProductOptionGroupViewResult> getProductOptionGroups(Long ceoId, Long shopId);

    List<ProductOptionGroupLinkedProductResult> getLinkedProducts(Long ceoId, Long shopId, Long optionGroupId);

    List<ProductOptionGroupLinkedProductsResult> getLinkedProductsByShop(Long ceoId, Long shopId);
}
