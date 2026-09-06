package com.tastyhouse.infrastructure.product.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "PRODUCT_BBQ")
public class ProductBbqJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false, unique = true)
    private Long productId;

    @Column(name = "bbq_menu_id", nullable = false)
    private Long bbqMenuId;

    @Column(name = "bbq_category_id")
    private Long bbqCategoryId;

    @Column(name = "is_options_synced", nullable = false)
    private boolean optionsSynced;

    protected ProductBbqJpaEntity() {
    }

    private ProductBbqJpaEntity(Long productId, Long bbqMenuId, Long bbqCategoryId, boolean optionsSynced) {
        this.productId = productId;
        this.bbqMenuId = bbqMenuId;
        this.bbqCategoryId = bbqCategoryId;
        this.optionsSynced = optionsSynced;
    }

    static ProductBbqJpaEntity create(Long productId, Long bbqMenuId, Long bbqCategoryId, boolean optionsSynced) {
        return new ProductBbqJpaEntity(productId, bbqMenuId, bbqCategoryId, optionsSynced);
    }

    void applyChanges(boolean optionsSynced) {
        this.optionsSynced = optionsSynced;
    }

    public Long getId() {
        return this.id;
    }

    public Long getProductId() {
        return this.productId;
    }

    public Long getBbqMenuId() {
        return this.bbqMenuId;
    }

    public Long getBbqCategoryId() {
        return this.bbqCategoryId;
    }

    public boolean isOptionsSynced() {
        return this.optionsSynced;
    }
}
