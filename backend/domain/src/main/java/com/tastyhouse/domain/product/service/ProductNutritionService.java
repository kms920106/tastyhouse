package com.tastyhouse.domain.product.service;

import java.util.List;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.product.model.AllergenType;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductAllergen;
import com.tastyhouse.domain.product.model.ProductNutrition;
import com.tastyhouse.domain.product.repository.ProductAllergenRepository;
import com.tastyhouse.domain.product.repository.ProductNutritionRepository;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.vo.ProductId;

public class ProductNutritionService {
    private final ProductNutritionRepository productNutritionRepository;
    private final ProductAllergenRepository productAllergenRepository;
    private final ProductRepository productRepository;

    public ProductNutritionService(
        ProductNutritionRepository productNutritionRepository,
        ProductAllergenRepository productAllergenRepository,
        ProductRepository productRepository
    ) {
        this.productNutritionRepository = productNutritionRepository;
        this.productAllergenRepository = productAllergenRepository;
        this.productRepository = productRepository;
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

        ProductNutrition existing = productNutritionRepository.findByProductId(productId).orElse(null);
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

        productNutritionRepository.save(productNutrition);
        replaceAllergens(productId, allergenTypes);
    }

    public void deleteNutrition(ProductId productId) {
        ProductNutrition productNutrition = productNutritionRepository.findByProductId(productId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NUTRITION_NOT_FOUND));

        productAllergenRepository.deleteAllByProductId(productId);
        productNutritionRepository.delete(productNutrition);
    }

    private void replaceAllergens(ProductId productId, List<AllergenType> allergenTypes) {
        productAllergenRepository.deleteAllByProductId(productId);

        if (allergenTypes == null || allergenTypes.isEmpty()) {
            return;
        }

        List<ProductAllergen> allergens = allergenTypes.stream()
            .distinct()
            .map(allergenType -> ProductAllergen.of(productId, allergenType))
            .toList();
        productAllergenRepository.saveAll(allergens);
    }

    private void validateProductExists(ProductId productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        if (product.isDeleted()) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }
    }
}
