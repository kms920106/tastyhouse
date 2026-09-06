package com.tastyhouse.domain.product.model;

import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;

public class ProductOptionGroup {
    private final Long id;
    private final ProductId productId;
    private String name;
    private String description;
    private boolean required;
    private boolean multipleSelect;
    private Integer minSelect;
    private Integer maxSelect;
    private Integer sort;
    private boolean visible;

    private final ProductOptionGroupType groupType;

    private ProductOptionGroup(
        Long id,
        ProductId productId,
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
        this.id = id;
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

    public static ProductOptionGroup of(
        ProductId productId,
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
        return new ProductOptionGroup(
            null, productId, name, description, required, multipleSelect,
            minSelect, maxSelect, sort, visible, groupType
        );
    }

    public static ProductOptionGroup reconstitute(
        Long id,
        ProductId productId,
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
        return new ProductOptionGroup(
            id, productId, name, description, required, multipleSelect,
            minSelect, maxSelect, sort, visible, groupType
        );
    }

    public ProductOptionGroupId getProductOptionGroupId() {
        return ProductOptionGroupId.of(this.id);
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

    public void hide() {
        this.visible = false;
    }

    public Long getId() {
        return this.id;
    }

    public ProductId getProductId() {
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

    public boolean isCupDeposit() {
        return this.groupType.isCupDeposit();
    }
}
