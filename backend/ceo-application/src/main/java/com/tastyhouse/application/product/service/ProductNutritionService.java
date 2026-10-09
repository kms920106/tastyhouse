package com.tastyhouse.application.product.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.product.model.AllergenType;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductAllergen;
import com.tastyhouse.domain.product.model.ProductNutrition;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.application.product.port.out.write.ProductAllergenSavePort;
import com.tastyhouse.application.product.port.out.write.ProductLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductNutritionLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductNutritionSavePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.CeoErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
public class ProductNutritionService {

    private final ProductNutritionLoadPort productNutritionLoadPort;
    private final ProductNutritionSavePort productNutritionSavePort;
    private final ProductAllergenSavePort productAllergenSavePort;
    private final ProductLoadPort productLoadPort;

    public ProductNutritionService(
        ProductNutritionLoadPort productNutritionLoadPort,
        ProductNutritionSavePort productNutritionSavePort,
        ProductAllergenSavePort productAllergenSavePort,
        ProductLoadPort productLoadPort
    ) {
        this.productNutritionLoadPort = productNutritionLoadPort;
        this.productNutritionSavePort = productNutritionSavePort;
        this.productAllergenSavePort = productAllergenSavePort;
        this.productLoadPort = productLoadPort;
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

        ProductNutrition existing = productNutritionLoadPort.findByProductId(productId).orElse(null);
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

        productNutritionSavePort.save(productNutrition);
        replaceAllergens(productId, allergenTypes);
    }

    public void deleteNutrition(ProductId productId) {
        ProductNutrition productNutrition = productNutritionLoadPort.findByProductId(productId)
            .orElseThrow(() -> new ResourceNotFoundException(CeoErrorCode.PRODUCT_NUTRITION_NOT_FOUND));

        productAllergenSavePort.deleteAllByProductId(productId);
        productNutritionSavePort.delete(productNutrition);
    }

    private void replaceAllergens(ProductId productId, List<AllergenType> allergenTypes) {
        productAllergenSavePort.deleteAllByProductId(productId);

        if (allergenTypes == null || allergenTypes.isEmpty()) {
            return;
        }

        List<ProductAllergen> allergens = allergenTypes.stream()
            .distinct()
            .map(allergenType -> ProductAllergen.of(productId, allergenType))
            .toList();
        productAllergenSavePort.saveAll(allergens);
    }

    private void validateProductExists(ProductId productId) {
        Product product = productLoadPort.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.PRODUCT_NOT_FOUND));
        if (product.isDeleted()) {
            throw new ResourceNotFoundException(ApplicationErrorCode.PRODUCT_NOT_FOUND);
        }
    }
}
