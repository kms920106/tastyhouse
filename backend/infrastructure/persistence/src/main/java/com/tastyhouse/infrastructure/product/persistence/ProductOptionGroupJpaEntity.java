package com.tastyhouse.infrastructure.product.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.product.model.ProductOptionGroupType;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "PRODUCT_OPTION_GROUP")
public class ProductOptionGroupJpaEntity extends BaseEntity {
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

    @Enumerated(EnumType.STRING)
    @Column(name = "group_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private ProductOptionGroupType groupType;

    protected ProductOptionGroupJpaEntity() {
    }

    private ProductOptionGroupJpaEntity(
        Long productId,
        String name,
        String description,
        boolean required,
        boolean multipleSelect,
        Integer minSelect,
        Integer maxSelect,
        Integer sort,
        boolean visible,
        ProductOptionGroupType groupType
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
        this.groupType = groupType != null ? groupType : ProductOptionGroupType.NORMAL;
    }

    static ProductOptionGroupJpaEntity create(
        Long productId,
        String name,
        String description,
        boolean required,
        boolean multipleSelect,
        Integer minSelect,
        Integer maxSelect,
        Integer sort,
        boolean visible,
        ProductOptionGroupType groupType
    ) {
        return new ProductOptionGroupJpaEntity(
            productId, name, description, required, multipleSelect, minSelect, maxSelect, sort, visible,
            groupType
        );
    }

    void applyChanges(
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

    public Long getId() {
        return this.id;
    }

    public Long getProductId() {
        return this.productId;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public boolean isRequired() {
        return this.required;
    }

    public boolean isMultipleSelect() {
        return this.multipleSelect;
    }

    public Integer getMinSelect() {
        return this.minSelect;
    }

    public Integer getMaxSelect() {
        return this.maxSelect;
    }

    public Integer getSort() {
        return this.sort;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public ProductOptionGroupType getGroupType() {
        return this.groupType;
    }
}
