package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public interface ProductNutritionCommandUseCase {

    void updateNutrition(ProductNutritionUpdateCommand command);

    void deleteNutrition(ProductNutritionDeleteCommand command);
}
