package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.application.product.port.out.ProductOptionGroupViewResult;

public interface ProductOptionGroupListQueryUseCase {

    List<ProductOptionGroupViewResult> getProductOptionGroups(Long ceoId, Long shopId);
}
