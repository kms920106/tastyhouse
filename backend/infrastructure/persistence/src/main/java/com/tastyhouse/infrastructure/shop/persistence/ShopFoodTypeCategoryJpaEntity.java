package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.shop.model.FoodType;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "SHOP_FOOD_TYPE_CATEGORY")
public class ShopFoodTypeCategoryJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "food_type", nullable = false, unique = true, length = 50, columnDefinition = "VARCHAR(50)")
    private FoodType foodType;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "active_image_file_id", nullable = false)
    private Long activeImageFileId;

    @Column(name = "inactive_image_file_id", nullable = false)
    private Long inactiveImageFileId;

    @Column(name = "sort", nullable = false)
    private Integer sort;

    @Column(name = "is_visible", nullable = false)
    private boolean visible;

    protected ShopFoodTypeCategoryJpaEntity() {
    }

    private ShopFoodTypeCategoryJpaEntity(
        FoodType foodType,
        String displayName,
        Long activeImageFileId,
        Long inactiveImageFileId,
        Integer sort,
        boolean visible
    ) {
        this.foodType = foodType;
        this.displayName = displayName;
        this.activeImageFileId = activeImageFileId;
        this.inactiveImageFileId = inactiveImageFileId;
        this.sort = sort;
        this.visible = visible;
    }

    static ShopFoodTypeCategoryJpaEntity create(
        FoodType foodType,
        String displayName,
        Long activeImageFileId,
        Long inactiveImageFileId,
        Integer sort,
        boolean visible
    ) {
        return new ShopFoodTypeCategoryJpaEntity(foodType, displayName, activeImageFileId, inactiveImageFileId, sort, visible);
    }

    void applyChanges(String displayName, Long activeImageFileId, Long inactiveImageFileId, Integer sort, boolean visible) {
        this.displayName = displayName;
        this.activeImageFileId = activeImageFileId;
        this.inactiveImageFileId = inactiveImageFileId;
        this.sort = sort;
        this.visible = visible;
    }

    public Long getId() {
        return this.id;
    }

    public FoodType getFoodType() {
        return this.foodType;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public Long getActiveImageFileId() {
        return this.activeImageFileId;
    }

    public Long getInactiveImageFileId() {
        return this.inactiveImageFileId;
    }

    public Integer getSort() {
        return this.sort;
    }

    public boolean isVisible() {
        return this.visible;
    }
}
