package com.tastyhouse.core.entity.place;

import com.tastyhouse.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "PLACE_FOOD_TYPE_CATEGORY")
public class PlaceFoodTypeCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "food_type", nullable = false, unique = true, length = 50, columnDefinition = "VARCHAR(50)")
    private FoodType foodType;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "sort", nullable = false)
    private Integer sort;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    private PlaceFoodTypeCategory(FoodType foodType, String displayName, String imageUrl, Integer sort, Boolean isActive) {
        this.foodType = foodType;
        this.displayName = displayName;
        this.imageUrl = imageUrl;
        this.sort = sort;
        this.isActive = isActive;
    }

    public static PlaceFoodTypeCategory of(
        FoodType foodType,
        String displayName,
        String imageUrl,
        Integer sort,
        Boolean isActive
    ) {
        return new PlaceFoodTypeCategory(
            foodType,
            displayName,
            imageUrl,
            sort,
            isActive
        );
    }

    public void update(
        String displayName,
        String imageUrl,
        Integer sort,
        Boolean isActive
    ) {
        this.displayName = displayName;
        this.imageUrl = imageUrl;
        this.sort = sort;
        this.isActive = isActive;
    }
}
