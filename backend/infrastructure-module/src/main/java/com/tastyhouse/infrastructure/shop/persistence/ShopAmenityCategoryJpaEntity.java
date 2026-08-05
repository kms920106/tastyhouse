package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.shop.model.Amenity;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 편의시설 카테고리 JPA 영속 모델. 순수 도메인 모델 {@code ShopAmenityCategory}와 분리된 영속 전용 엔티티다.
 */
@Entity
@Table(name = "SHOP_AMENITY_CATEGORY")
public class ShopAmenityCategoryJpaEntity extends BaseEntity {

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

    @Column(name = "is_visible", nullable = false)
    private boolean visible; // 사용 여부 (true: 사용 중)

    protected ShopAmenityCategoryJpaEntity() {
    }

    private ShopAmenityCategoryJpaEntity(
        Amenity amenity,
        String displayName,
        Long activeImageFileId,
        Long inactiveImageFileId,
        Integer sort,
        boolean visible
    ) {
        this.amenity = amenity;
        this.displayName = displayName;
        this.activeImageFileId = activeImageFileId;
        this.inactiveImageFileId = inactiveImageFileId;
        this.sort = sort;
        this.visible = visible;
    }

    static ShopAmenityCategoryJpaEntity create(
        Amenity amenity,
        String displayName,
        Long activeImageFileId,
        Long inactiveImageFileId,
        Integer sort,
        boolean visible
    ) {
        return new ShopAmenityCategoryJpaEntity(amenity, displayName, activeImageFileId, inactiveImageFileId, sort, visible);
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

    public Amenity getAmenity() {
        return this.amenity;
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
