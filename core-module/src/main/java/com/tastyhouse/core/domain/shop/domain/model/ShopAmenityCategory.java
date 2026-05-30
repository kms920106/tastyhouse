package com.tastyhouse.core.domain.shop.domain.model;

import com.tastyhouse.core.shared.entity.BaseEntity;
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
@Table(name = "SHOP_AMENITY_CATEGORY")
public class ShopAmenityCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Enumerated(EnumType.STRING)
    @Column(name = "amenity", nullable = false, unique = true, length = 50, columnDefinition = "VARCHAR(50)")
    private Amenity amenity; // 편의시설 유형 (WIFI, PARKING, PET_FRIENDLY 등)

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

    private ShopAmenityCategory(
        Amenity amenity,
        String displayName,
        Long activeImageFileId,
        Long inactiveImageFileId,
        Integer sort,
        Boolean isActive
    ) {
        this.amenity = amenity;
        this.displayName = displayName;
        this.activeImageFileId = activeImageFileId;
        this.inactiveImageFileId = inactiveImageFileId;
        this.sort = sort;
        this.isActive = isActive;
    }

    public static ShopAmenityCategory of(
        Amenity amenity,
        String displayName,
        Long activeImageFileId,
        Long inactiveImageFileId,
        Integer sort,
        Boolean isActive
    ) {
        return new ShopAmenityCategory(
            amenity,
            displayName,
            activeImageFileId,
            inactiveImageFileId,
            sort,
            isActive
        );
    }

    public void update(String displayName, Long activeImageFileId, Long inactiveImageFileId, Integer sort, Boolean isActive) {
        this.displayName = displayName;
        this.activeImageFileId = activeImageFileId;
        this.inactiveImageFileId = inactiveImageFileId;
        this.sort = sort;
        this.isActive = isActive;
    }
}
