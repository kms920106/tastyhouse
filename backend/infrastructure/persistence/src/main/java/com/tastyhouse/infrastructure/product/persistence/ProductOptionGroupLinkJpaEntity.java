package com.tastyhouse.infrastructure.product.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "PRODUCT_OPTION_GROUP_LINK")
public class ProductOptionGroupLinkJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "option_group_id", nullable = false)
    private Long optionGroupId;

    @Column(name = "sort", nullable = false)
    private Integer sort;

    protected ProductOptionGroupLinkJpaEntity() {
    }

    private ProductOptionGroupLinkJpaEntity(Long productId, Long optionGroupId, Integer sort) {
        this.productId = productId;
        this.optionGroupId = optionGroupId;
        this.sort = sort;
    }

    static ProductOptionGroupLinkJpaEntity create(Long productId, Long optionGroupId, Integer sort) {
        return new ProductOptionGroupLinkJpaEntity(productId, optionGroupId, sort);
    }

    void applyChanges(Integer sort) {
        this.sort = sort;
    }

    public Long getId() {
        return this.id;
    }

    public Long getProductId() {
        return this.productId;
    }

    public Long getOptionGroupId() {
        return this.optionGroupId;
    }

    public Integer getSort() {
        return this.sort;
    }
}
