package com.tastyhouse.infrastructure.product.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "PRODUCT_NUTRITION")
public class ProductNutritionJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "serving_size", length = 50)
    private String servingSize;

    @Column(name = "total_amount", length = 50)
    private String totalAmount;

    @Column(name = "flavor", length = 50)
    private String flavor;

    @Column(name = "size", length = 50)
    private String size;

    @Column(name = "calorie")
    private Integer calorie;

    @Column(name = "sugars")
    private Integer sugars;

    @Column(name = "protein")
    private Integer protein;

    @Column(name = "saturated_fat")
    private Integer saturatedFat;

    @Column(name = "natrium")
    private Integer natrium;

    @Column(name = "carbohydrate")
    private Integer carbohydrate;

    @Column(name = "cholesterol")
    private Integer cholesterol;

    @Column(name = "fat")
    private Integer fat;

    @Column(name = "trans_fat")
    private Integer transFat;

    @Column(name = "caffeine")
    private Integer caffeine;

    @Column(name = "is_set_menu", nullable = false)
    private boolean setMenu;

    protected ProductNutritionJpaEntity() {
    }

    private ProductNutritionJpaEntity(
        Long productId,
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
    }

    static ProductNutritionJpaEntity create(
        Long productId,
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
        return new ProductNutritionJpaEntity(productId, servingSize, totalAmount, flavor, size,
            calorie, sugars, protein, saturatedFat, natrium,
            carbohydrate, cholesterol, fat, transFat, caffeine, setMenu);
    }

    void applyChanges(
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

    public Long getId() {
        return this.id;
    }

    public Long getProductId() {
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
}
