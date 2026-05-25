package com.tastyhouse.core.domain.place.domain.model;

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

    @Column(name = "is_active", nullable = false)
    private Boolean isActive; // 사용 여부 (true: 사용 중)

    private PlaceFoodTypeCategory(FoodType foodType, String displayName, Long activeImageFileId, Long inactiveImageFileId, Integer sort, Boolean isActive) {
        this.foodType = foodType;
        this.displayName = displayName;
        this.activeImageFileId = activeImageFileId;
        this.inactiveImageFileId = inactiveImageFileId;
        this.sort = sort;
        this.isActive = isActive;
    }

    public static PlaceFoodTypeCategory of(
        FoodType foodType,
        String displayName,
        Long activeImageFileId,
        Long inactiveImageFileId,
        Integer sort,
        Boolean isActive
    ) {
        return new PlaceFoodTypeCategory(
            foodType,
            displayName,
            activeImageFileId,
            inactiveImageFileId,
            sort,
            isActive
        );
    }
}
