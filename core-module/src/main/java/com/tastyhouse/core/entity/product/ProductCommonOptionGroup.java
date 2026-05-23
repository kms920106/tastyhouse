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
@Table(name = "PRODUCT_COMMON_OPTION_GROUP")
public class ProductCommonOptionGroup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "product_id", nullable = false)
    private Long productId; // 상품 ID (PRODUCT.id 참조)

    @Column(name = "name", nullable = false, length = 100)
    private String name; // 공통 옵션 그룹명

    @Column(name = "description", length = 500)
    private String description; // 공통 옵션 그룹 설명

    @Column(name = "is_required", nullable = false)
    private Boolean isRequired; // 필수 선택 여부 (true: 필수)

    @Column(name = "is_multiple_select", nullable = false)
    private Boolean isMultipleSelect; // 다중 선택 허용 여부 (true: 다중 선택 가능)

    @Column(name = "min_select")
    private Integer minSelect; // 최소 선택 수

    @Column(name = "max_select")
    private Integer maxSelect; // 최대 선택 수

    @Column(name = "sort", nullable = false)
    private Integer sort; // 정렬 순서

    @Column(name = "is_active", nullable = false)
    private Boolean isActive; // 활성화 여부 (true: 활성)

    private ProductCommonOptionGroup(
        Long productId,
        String name,
        String description,
        Boolean isRequired,
        Boolean isMultipleSelect,
        Integer minSelect,
        Integer maxSelect,
        Integer sort,
        Boolean isActive
    ) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.isRequired = isRequired != null ? isRequired : false;
        this.isMultipleSelect = isMultipleSelect != null ? isMultipleSelect : false;
        this.minSelect = minSelect;
        this.maxSelect = maxSelect;
        this.sort = sort;
        this.isActive = isActive != null ? isActive : true;
    }

    public static ProductCommonOptionGroup of(
        Long productId,
        String name,
        String description,
        Boolean isRequired,
        Boolean isMultipleSelect,
        Integer minSelect,
        Integer maxSelect,
        Integer sort,
        Boolean isActive
    ) {
        return new ProductCommonOptionGroup(
            productId,
            name,
            description,
            isRequired,
            isMultipleSelect,
            minSelect,
            maxSelect,
            sort,
            isActive
        );
    }

    public void update(
        String name,
        String description,
        Boolean isRequired,
        Boolean isMultipleSelect,
        Integer minSelect,
        Integer maxSelect,
        Integer sort,
        Boolean isActive
    ) {
        this.name = name;
        this.description = description;
        this.isRequired = isRequired;
        this.isMultipleSelect = isMultipleSelect;
        this.minSelect = minSelect;
        this.maxSelect = maxSelect;
        this.sort = sort;
        this.isActive = isActive;
    }
}
