package com.tastyhouse.core.domain.product.domain.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.core.domain.product.domain.vo.ProductDiscountInfo;
import com.tastyhouse.core.shared.entity.BaseEntity;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "PRODUCT")
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "product_category_id")
    private Long productCategoryId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "original_price", nullable = false)
    private Integer originalPrice;

    @Embedded
    private ProductDiscountInfo discountInfo;

    @Column(name = "rating")
    private Double rating;

    @Column(name = "review_count")
    private Integer reviewCount;

    @Column(name = "is_representative")
    private boolean representative;

    @Column(name = "spiciness")
    private Integer spiciness;

    @Column(name = "is_sold_out", nullable = false)
    private boolean soldOut;

    @Column(name = "is_visible", nullable = false)
    private boolean visible;

    @Column(name = "sort", nullable = false)
    private Integer sort;

    private Product(
        Long shopId,
        Long productCategoryId,
        String name,
        String description,
        Integer originalPrice,
        Integer discountPrice,
        BigDecimal discountRate,
        Double rating,
        Integer reviewCount,
        boolean representative,
        Integer spiciness,
        boolean soldOut,
        boolean visible,
        Integer sort
    ) {
        this.shopId = shopId;
        this.productCategoryId = productCategoryId;
        this.name = name;
        this.description = description;
        this.originalPrice = originalPrice;
        this.discountInfo = ProductDiscountInfo.of(discountPrice, discountRate);
        this.rating = rating;
        this.reviewCount = reviewCount != null ? reviewCount : 0;
        this.representative = representative;
        this.spiciness = spiciness;
        this.soldOut = soldOut;
        this.visible = visible;
        this.sort = sort;
    }

    public static Product of(
        Long shopId,
        Long productCategoryId,
        String name,
        String description,
        Integer originalPrice,
        Integer discountPrice,
        BigDecimal discountRate,
        Double rating,
        Integer reviewCount,
        boolean representative,
        Integer spiciness,
        boolean soldOut,
        boolean visible,
        Integer sort
    ) {
        return new Product(
            shopId, productCategoryId, name, description,
            originalPrice, discountPrice, discountRate,
            rating, reviewCount, representative, spiciness,
            soldOut, visible, sort
        );
    }

    public Integer getDiscountPrice() {
        return discountInfo != null ? discountInfo.discountPrice() : null;
    }

    public BigDecimal getDiscountRate() {
        return discountInfo != null ? discountInfo.discountRate() : null;
    }

    public void updateReviewStats(Double rating, Integer reviewCount) {
        this.rating = rating;
        this.reviewCount = reviewCount;
    }

    public void markSoldOut() {
        this.soldOut = true;
    }

    public void deactivate() {
        this.visible = false;
    }

    public void update(
        Long productCategoryId,
        String name,
        String description,
        Integer originalPrice,
        Integer discountPrice,
        BigDecimal discountRate,
        boolean representative,
        Integer spiciness,
        boolean soldOut,
        boolean visible,
        Integer sort
    ) {
        this.productCategoryId = productCategoryId;
        this.name = name;
        this.description = description;
        this.originalPrice = originalPrice;
        this.discountInfo = ProductDiscountInfo.of(discountPrice, discountRate);
        this.representative = representative;
        this.spiciness = spiciness;
        this.soldOut = soldOut;
        this.visible = visible;
        this.sort = sort;
    }
}
