package com.tastyhouse.core.domain.product.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
    private boolean required;

    @Column(name = "is_multiple_select", nullable = false)
    private boolean multipleSelect;

    @Column(name = "min_select")
    private Integer minSelect;

    @Column(name = "max_select")
    private Integer maxSelect;

    @Column(name = "sort", nullable = false)
    private Integer sort;

    @Column(name = "is_visible", nullable = false)
    private boolean visible;

    private ProductOptionGroup(
        Long productId,
        String name,
        String description,
        boolean required,
        boolean multipleSelect,
        Integer minSelect,
        Integer maxSelect,
        Integer sort,
        boolean visible
    ) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.required = required;
        this.multipleSelect = multipleSelect;
        this.minSelect = minSelect;
        this.maxSelect = maxSelect;
        this.sort = sort;
        this.visible = visible;
    }

    public static ProductOptionGroup of(
        Long productId,
        String name,
        String description,
        boolean required,
        boolean multipleSelect,
        Integer minSelect,
        Integer maxSelect,
        Integer sort,
        boolean visible
    ) {
        return new ProductOptionGroup(
            productId, name, description, required, multipleSelect,
            minSelect, maxSelect, sort, visible
        );
    }

    public void update(
        String name,
        String description,
        boolean required,
        boolean multipleSelect,
        Integer minSelect,
        Integer maxSelect,
        Integer sort,
        boolean visible
    ) {
        this.name = name;
        this.description = description;
        this.required = required;
        this.multipleSelect = multipleSelect;
        this.minSelect = minSelect;
        this.maxSelect = maxSelect;
        this.sort = sort;
        this.visible = visible;
    }
}
