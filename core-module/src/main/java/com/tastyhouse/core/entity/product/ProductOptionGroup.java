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
@Table(name = "PRODUCT_OPTION_GROUP")
public class ProductOptionGroup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_required", nullable = false)
    private Boolean isRequired;

    @Column(name = "is_multiple_select", nullable = false)
    private Boolean isMultipleSelect;

    @Column(name = "min_select")
    private Integer minSelect;

    @Column(name = "max_select")
    private Integer maxSelect;

    @Column(name = "sort", nullable = false)
    private Integer sort;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    private ProductOptionGroup(
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

    public static ProductOptionGroup of(
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
        return new ProductOptionGroup(
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
