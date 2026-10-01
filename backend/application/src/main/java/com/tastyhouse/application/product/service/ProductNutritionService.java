package com.tastyhouse.application.product.service;

import java.util.List;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.product.model.AllergenType;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductAllergen;
import com.tastyhouse.domain.product.model.ProductNutrition;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.application.product.port.out.write.ProductAllergenPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductNutritionPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductPersistencePort;
import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public class ProductNutritionService {

    private final ProductNutritionPersistencePort productNutritionPersistencePort;
    private final ProductAllergenPersistencePort productAllergenPersistencePort;
    private final ProductPersistencePort productPersistencePort;

    public ProductNutritionService(
        ProductNutritionPersistencePort productNutritionPersistencePort,
        ProductAllergenPersistencePort productAllergenPersistencePort,
        ProductPersistencePort productPersistencePort
    ) {
        this.productNutritionPersistencePort = productNutritionPersistencePort;
        this.productAllergenPersistencePort = productAllergenPersistencePort;
        this.productPersistencePort = productPersistencePort;
    }

    public void upsertNutrition(
        ProductId productId,
        String servingSize,
        String totalAmount,
        String flavor,
        String size,
        Integer calorie,
        Integer sugars,
        Integer protein,
        Integer saturatedFat,
        Integer natrium,
        Integer carbohydrate,
        Integer cholesterol,
        Integer fat,
        Integer transFat,
        Integer caffeine,
        boolean setMenu,
        List<AllergenType> allergenTypes
    ) {
        validateProductExists(productId);

        ProductNutrition existing = productNutritionPersistencePort.findByProductId(productId).orElse(null);
        ProductNutrition productNutrition;
        if (existing == null) {
            productNutrition = ProductNutrition.of(productId, servingSize, totalAmount, flavor, size,
                calorie, sugars, protein, saturatedFat, natrium,
                carbohydrate, cholesterol, fat, transFat, caffeine, setMenu);
        } else {
            existing.update(servingSize, totalAmount, flavor, size,
                calorie, sugars, protein, saturatedFat, natrium,
                carbohydrate, cholesterol, fat, transFat, caffeine, setMenu);
            productNutrition = existing;
        }

        productNutritionPersistencePort.save(productNutrition);
        replaceAllergens(productId, allergenTypes);
    }

    public void deleteNutrition(ProductId productId) {
        ProductNutrition productNutrition = productNutritionPersistencePort.findByProductId(productId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NUTRITION_NOT_FOUND));

        productAllergenPersistencePort.deleteAllByProductId(productId);
        productNutritionPersistencePort.delete(productNutrition);
    }

    private void replaceAllergens(ProductId productId, List<AllergenType> allergenTypes) {
        productAllergenPersistencePort.deleteAllByProductId(productId);

        if (allergenTypes == null || allergenTypes.isEmpty()) {
            return;
        }

        List<ProductAllergen> allergens = allergenTypes.stream()
            .distinct()
            .map(allergenType -> ProductAllergen.of(productId, allergenType))
            .toList();
        productAllergenPersistencePort.saveAll(allergens);
    }

    private void validateProductExists(ProductId productId) {
        Product product = productPersistencePort.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        if (product.isDeleted()) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }
    }
}
