package com.tastyhouse.core.domain.product.domain.model;

import com.tastyhouse.core.shared.entity.BaseEntity;
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

    @Column(name = "is_visible", nullable = false)
    private boolean visible;

    @Column(name = "image_file_id", nullable = false)
    private Long imageFileId;

    private ProductImage(Long productId, Long imageFileId, Integer sort, boolean visible) {
        this.productId = productId;
        this.imageFileId = imageFileId;
        this.sort = sort;
        this.visible = visible;
    }

    public static ProductImage of(Long productId, Long imageFileId, Integer sort, boolean visible) {
        return new ProductImage(productId, imageFileId, sort, visible);
    }
}
