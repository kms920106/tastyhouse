package com.tastyhouse.core.entity.product;

import com.tastyhouse.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "PRODUCT_CATEGORY")
public class ProductCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "place_id", nullable = false)
    private Long placeId; // 매장 ID (PLACE.id 참조)

    @Column(name = "name", nullable = false, length = 100)
    private String name; // 카테고리명

    @Column(name = "sort", nullable = false)
    private Integer sort; // 정렬 순서

    @Column(name = "is_active", nullable = false)
    private Boolean isActive; // 활성화 여부 (true: 활성)

    private ProductCategory(
        Long placeId,
        String name,
        Integer sort,
        Boolean isActive
    ) {
        this.placeId = placeId;
        this.name = name;
        this.sort = sort;
        this.isActive = isActive != null ? isActive : true;
    }

    public static ProductCategory of(
        Long placeId,
        String name,
        Integer sort,
        Boolean isActive
    ) {
        return new ProductCategory(
            placeId,
            name,
            sort,
            isActive
        );
    }

    public void update(
        String displayName,
        Integer sort,
        Boolean isActive
    ) {
        this.name = displayName;
        this.sort = sort;
        this.isActive = isActive;
    }
}
