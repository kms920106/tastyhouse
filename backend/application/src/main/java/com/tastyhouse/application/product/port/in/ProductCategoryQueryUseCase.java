package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import com.tastyhouse.application.product.port.out.ProductCategoryManagementResult;

@CeoApp
public interface ProductCategoryQueryUseCase {

    List<ProductCategoryManagementResult> getProductCategories(Long ceoId, Long shopId);
}
