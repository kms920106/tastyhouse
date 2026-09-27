package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.application.product.port.out.ProductCategoryManagementResult;
import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public interface ProductCategoryQueryUseCase {

    List<ProductCategoryManagementResult> getProductCategories(Long ceoId, Long shopId);
}
