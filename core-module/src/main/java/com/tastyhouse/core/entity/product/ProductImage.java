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
@Table(name = "PRODUCT_IMAGE")
public class ProductImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "sort", nullable = false)
    private Integer sort;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    private ProductImage(
        Long productId,
        String imageUrl,
        Integer sort,
        Boolean isActive
    ) {
        this.productId = productId;
        this.imageUrl = imageUrl;
        this.sort = sort;
        this.isActive = isActive != null ? isActive : true;
    }

    public static ProductImage of(
        Long productId,
        String imageUrl,
        Integer sort,
        Boolean isActive) {
        return new ProductImage(
            productId,
            imageUrl,
            sort,
            isActive
        );
    }

    public void update(
        String imageUrl,
        Integer sort,
        Boolean isActive
    ) {
        this.imageUrl = imageUrl;
        this.sort = sort;
        this.isActive = isActive;
    }
}
