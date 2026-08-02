package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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

import com.tastyhouse.domain.shop.domain.model.FoodType;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;
import com.tastyhouse.domain.file.domain.vo.UploadedFileId;
import com.tastyhouse.infrastructure.file.persistence.UploadedFileIdConverter;

/**
 * 음식 유형 카테고리 JPA 영속 모델. 순수 도메인 모델 {@code ShopFoodTypeCategory}와 분리된 영속 전용 엔티티다.
 */
@Getter
@Entity
@Table(name = "SHOP_FOOD_TYPE_CATEGORY")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShopFoodTypeCategoryJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Enumerated(EnumType.STRING)
    @Column(name = "food_type", nullable = false, unique = true, length = 50, columnDefinition = "VARCHAR(50)")
    private FoodType foodType; // 음식 유형 (KOREAN, JAPANESE, CHINESE, WESTERN 등)

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName; // 화면 표시명

    @Convert(converter = UploadedFileIdConverter.class)
    @Column(name = "active_image_file_id", nullable = false)
    private UploadedFileId activeImageFileId; // 활성 상태 아이콘 파일 ID (FILE.id 참조)

    @Convert(converter = UploadedFileIdConverter.class)
    @Column(name = "inactive_image_file_id", nullable = false)
    private UploadedFileId inactiveImageFileId; // 비활성 상태 아이콘 파일 ID (FILE.id 참조)

    @Column(name = "sort", nullable = false)
    private Integer sort; // 정렬 순서

    @Column(name = "is_visible", nullable = false)
    private boolean visible; // 사용 여부 (true: 사용 중)

    private ShopFoodTypeCategoryJpaEntity(
        FoodType foodType,
        String displayName,
        UploadedFileId activeImageFileId,
        UploadedFileId inactiveImageFileId,
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
        UploadedFileId activeImageFileId,
        UploadedFileId inactiveImageFileId,
        Integer sort,
        boolean visible
    ) {
        return new ShopFoodTypeCategoryJpaEntity(foodType, displayName, activeImageFileId, inactiveImageFileId, sort, visible);
    }

    void applyChanges(String displayName, UploadedFileId activeImageFileId, UploadedFileId inactiveImageFileId, Integer sort, boolean visible) {
        this.displayName = displayName;
        this.activeImageFileId = activeImageFileId;
        this.inactiveImageFileId = inactiveImageFileId;
        this.sort = sort;
        this.visible = visible;
    }
}
