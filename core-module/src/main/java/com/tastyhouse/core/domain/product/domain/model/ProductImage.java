package com.tastyhouse.core.domain.product.domain.model;

import com.tastyhouse.core.common.BaseEntity;
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
@Table(name = "PRODUCT_IMAGE")
public class ProductImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "sort", nullable = false)
    private Integer sort;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "image_file_id", nullable = false)
    private Long imageFileId;

    private ProductImage(Long productId, Long imageFileId, Integer sort, Boolean isActive) {
        this.productId = productId;
        this.imageFileId = imageFileId;
        this.sort = sort;
        this.isActive = isActive != null ? isActive : true;
    }

    public static ProductImage of(Long productId, Long imageFileId, Integer sort, Boolean isActive) {
        return new ProductImage(productId, imageFileId, sort, isActive);
    }
}
