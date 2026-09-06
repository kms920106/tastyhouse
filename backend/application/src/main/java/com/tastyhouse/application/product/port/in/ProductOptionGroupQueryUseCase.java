package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import com.tastyhouse.application.product.port.out.ProductOptionGroupViewResult;
import com.tastyhouse.application.product.port.out.ProductOptionGroupLinkedProductResult;
import com.tastyhouse.application.product.port.out.ProductOptionGroupLinkedProductsResult;

@CeoApp
public interface ProductOptionGroupQueryUseCase {

    List<ProductOptionGroupViewResult> getProductOptionGroups(Long ceoId, Long shopId);

    List<ProductOptionGroupLinkedProductResult> getLinkedProducts(Long ceoId, Long shopId, Long optionGroupId);

    List<ProductOptionGroupLinkedProductsResult> getLinkedProductsByShop(Long ceoId, Long shopId);
}
