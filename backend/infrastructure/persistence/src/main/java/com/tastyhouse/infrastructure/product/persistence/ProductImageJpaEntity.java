package com.tastyhouse.infrastructure.product.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "PRODUCT_IMAGE")
public class ProductImageJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "sort", nullable = false)
    private Integer sort;

    @Column(name = "is_visible", nullable = false)
    private boolean visible;

    @Column(name = "image_file_id", nullable = false)
    private Long imageFileId;

    protected ProductImageJpaEntity() {
    }

    private ProductImageJpaEntity(Long productId, Long imageFileId, Integer sort, boolean visible) {
        this.productId = productId;
        this.imageFileId = imageFileId;
        this.sort = sort;
        this.visible = visible;
    }

    static ProductImageJpaEntity create(Long productId, Long imageFileId, Integer sort, boolean visible) {
        return new ProductImageJpaEntity(productId, imageFileId, sort, visible);
    }

    void applyChanges(Integer sort, boolean visible) {
        this.sort = sort;
        this.visible = visible;
    }

    public Long getId() {
        return this.id;
    }

    public Long getProductId() {
        return this.productId;
    }

    public Integer getSort() {
        return this.sort;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public Long getImageFileId() {
        return this.imageFileId;
    }
}
