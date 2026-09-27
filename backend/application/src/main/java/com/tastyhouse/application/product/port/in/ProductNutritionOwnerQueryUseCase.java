package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.application.product.port.out.ProductAllergenTypeView;
import com.tastyhouse.application.product.port.out.ProductNutritionViewResult;
import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public interface ProductNutritionOwnerQueryUseCase {

    ProductNutritionViewResult getNutrition(Long ceoId, Long shopId, Long productId);

    List<ProductAllergenTypeView> getAllergenTypes();
}
