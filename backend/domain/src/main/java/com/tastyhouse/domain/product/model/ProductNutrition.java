package com.tastyhouse.domain.product.model;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.product.vo.ProductId;

public class ProductNutrition {
    private final Long id;
    private final ProductId productId;
    private String servingSize;
    private String totalAmount;
    private String flavor;
    private String size;
    private Integer calorie;
    private Integer sugars;
    private Integer protein;
    private Integer saturatedFat;
    private Integer natrium;
    private Integer carbohydrate;
    private Integer cholesterol;
    private Integer fat;
    private Integer transFat;
    private Integer caffeine;
    private boolean setMenu;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private ProductNutrition(
        Long id,
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
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.productId = productId;
        this.servingSize = servingSize;
        this.totalAmount = totalAmount;
        this.flavor = flavor;
        this.size = size;
        this.calorie = calorie;
        this.sugars = sugars;
        this.protein = protein;
        this.saturatedFat = saturatedFat;
        this.natrium = natrium;
        this.carbohydrate = carbohydrate;
        this.cholesterol = cholesterol;
        this.fat = fat;
        this.transFat = transFat;
        this.caffeine = caffeine;
        this.setMenu = setMenu;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ProductNutrition of(
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
        boolean setMenu
    ) {
        validate(calorie, sugars, protein, saturatedFat, natrium,
            carbohydrate, cholesterol, fat, transFat, caffeine);

        return new ProductNutrition(null, productId, servingSize, totalAmount, flavor, size,
            calorie, sugars, protein, saturatedFat, natrium,
            carbohydrate, cholesterol, fat, transFat, caffeine, setMenu, null, null);
    }

    public static ProductNutrition reconstitute(
        Long id,
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
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ProductNutrition(id, productId, servingSize, totalAmount, flavor, size,
            calorie, sugars, protein, saturatedFat, natrium,
            carbohydrate, cholesterol, fat, transFat, caffeine, setMenu, createdAt, updatedAt);
    }

    public void update(
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
        boolean setMenu
    ) {
        validate(calorie, sugars, protein, saturatedFat, natrium,
            carbohydrate, cholesterol, fat, transFat, caffeine);

        this.servingSize = servingSize;
        this.totalAmount = totalAmount;
        this.flavor = flavor;
        this.size = size;
        this.calorie = calorie;
        this.sugars = sugars;
        this.protein = protein;
        this.saturatedFat = saturatedFat;
        this.natrium = natrium;
        this.carbohydrate = carbohydrate;
        this.cholesterol = cholesterol;
        this.fat = fat;
        this.transFat = transFat;
        this.caffeine = caffeine;
        this.setMenu = setMenu;
    }

    private static void validate(
        Integer calorie,
        Integer sugars,
        Integer protein,
        Integer saturatedFat,
        Integer natrium,
        Integer carbohydrate,
        Integer cholesterol,
        Integer fat,
        Integer transFat,
        Integer caffeine
    ) {
        validateRequiredTogether(calorie, sugars, protein, saturatedFat, natrium);
        validateNonNegative(calorie, sugars, protein, saturatedFat, natrium,
            carbohydrate, cholesterol, fat, transFat, caffeine);
    }

    private static void validateRequiredTogether(Integer... requiredValues) {
        long filled = Arrays.stream(requiredValues).filter(Objects::nonNull).count();
        if (filled != 0 && filled != requiredValues.length) {
            throw new BusinessException(ErrorCode.PRODUCT_NUTRITION_REQUIRED_FIELD_MISSING);
        }
    }

    private static void validateNonNegative(Integer... values) {
        for (Integer value : values) {
            if (value != null && value < 0) {
                throw new BusinessException(ErrorCode.PRODUCT_NUTRITION_VALUE_NEGATIVE);
            }
        }
    }

    public Long getId() {
        return this.id;
    }

    public ProductId getProductId() {
        return this.productId;
    }

    public String getServingSize() {
        return this.servingSize;
    }

    public String getTotalAmount() {
        return this.totalAmount;
    }

    public String getFlavor() {
        return this.flavor;
    }

    public String getSize() {
        return this.size;
    }

    public Integer getCalorie() {
        return this.calorie;
    }

    public Integer getSugars() {
        return this.sugars;
    }

    public Integer getProtein() {
        return this.protein;
    }

    public Integer getSaturatedFat() {
        return this.saturatedFat;
    }

    public Integer getNatrium() {
        return this.natrium;
    }

    public Integer getCarbohydrate() {
        return this.carbohydrate;
    }

    public Integer getCholesterol() {
        return this.cholesterol;
    }

    public Integer getFat() {
        return this.fat;
    }

    public Integer getTransFat() {
        return this.transFat;
    }

    public Integer getCaffeine() {
        return this.caffeine;
    }

    public boolean isSetMenu() {
        return this.setMenu;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
