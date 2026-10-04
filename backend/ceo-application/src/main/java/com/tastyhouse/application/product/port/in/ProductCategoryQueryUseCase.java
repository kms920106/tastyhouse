package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.application.product.port.out.ProductCategoryManagementResult;

public interface ProductCategoryQueryUseCase {

    List<ProductCategoryManagementResult> getProductCategories(Long ceoId, Long shopId);
}
