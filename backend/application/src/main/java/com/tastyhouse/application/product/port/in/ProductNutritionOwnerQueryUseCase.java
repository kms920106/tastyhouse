package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import com.tastyhouse.application.product.port.out.ProductAllergenTypeView;
import com.tastyhouse.application.product.port.out.ProductNutritionViewResult;

@CeoApp
public interface ProductNutritionOwnerQueryUseCase {

    ProductNutritionViewResult getNutrition(Long ceoId, Long shopId, Long productId);

    List<ProductAllergenTypeView> getAllergenTypes();
}
