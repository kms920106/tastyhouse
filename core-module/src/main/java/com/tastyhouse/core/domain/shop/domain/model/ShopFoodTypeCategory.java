package com.tastyhouse.core.domain.shop.domain.model;

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

import com.tastyhouse.core.shared.entity.BaseEntity;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "SHOP_FOOD_TYPE_CATEGORY")
public class ShopFoodTypeCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Enumerated(EnumType.STRING)
    @Column(name = "food_type", nullable = false, unique = true, length = 50, columnDefinition = "VARCHAR(50)")
    private FoodType foodType; // 음식 유형 (KOREAN, JAPANESE, CHINESE, WESTERN 등)

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName; // 화면 표시명

    @Column(name = "active_image_file_id", nullable = false)
    private Long activeImageFileId; // 활성 상태 아이콘 파일 ID (FILE.id 참조)

    @Column(name = "inactive_image_file_id", nullable = false)
    private Long inactiveImageFileId; // 비활성 상태 아이콘 파일 ID (FILE.id 참조)

    @Column(name = "sort", nullable = false)
    private Integer sort; // 정렬 순서

    @Column(name = "is_visible", nullable = false)
    private boolean visible; // 사용 여부 (true: 사용 중)

    private ShopFoodTypeCategory(FoodType foodType, String displayName, Long activeImageFileId, Long inactiveImageFileId, Integer sort, boolean visible) {
        this.foodType = foodType;
        this.displayName = displayName;
        this.activeImageFileId = activeImageFileId;
        this.inactiveImageFileId = inactiveImageFileId;
        this.sort = sort;
        this.visible = visible;
    }

    public static ShopFoodTypeCategory of(
        FoodType foodType,
        String displayName,
        Long activeImageFileId,
        Long inactiveImageFileId,
        Integer sort,
        boolean visible
    ) {
        return new ShopFoodTypeCategory(
            foodType,
            displayName,
            activeImageFileId,
            inactiveImageFileId,
            sort,
            visible
        );
    }
}
