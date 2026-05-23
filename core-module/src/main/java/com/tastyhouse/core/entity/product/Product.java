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

import java.math.BigDecimal;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "PRODUCT")
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "place_id", nullable = false)
    private Long placeId; // 매장 ID (PLACE.id 참조)

    @Column(name = "product_category_id")
    private Long productCategoryId; // 상품 카테고리 ID (PRODUCT_CATEGORY.id 참조)

    @Column(name = "name", nullable = false)
    private String name; // 상품명

    @Column(name = "description", length = 1000)
    private String description; // 상품 설명

    @Column(name = "price", nullable = false)
    private Integer originalPrice; // 정가 (원)

    @Column(name = "discount_price")
    private Integer discountPrice; // 할인 후 판매가 (원)

    @Column(name = "discount_rate")
    private BigDecimal discountRate; // 할인율 (%)

    @Column(name = "rating")
    private Double rating; // 평균 평점

    @Column(name = "review_count")
    private Integer reviewCount; // 리뷰 수

    @Column(name = "is_representative")
    private Boolean isRepresentative; // 대표 상품 여부 (true: 대표 상품)

    @Column(name = "spiciness")
    private Integer spiciness; // 매운 맛 단계 (0: 맵지 않음 ~ 고단계: 매움)

    @Column(name = "is_sold_out", nullable = false)
    private Boolean isSoldOut; // 품절 여부 (true: 품절)

    @Column(name = "is_active", nullable = false)
    private Boolean isActive; // 활성화 여부 (true: 활성)

    @Column(name = "sort", nullable = false)
    private Integer sort; // 정렬 순서

    private Product(
        Long placeId,
        Long productCategoryId,
        String name,
        String description,
        Integer originalPrice,
        Integer discountPrice,
        BigDecimal discountRate,
        Double rating,
        Integer reviewCount,
        Boolean isRepresentative,
        Integer spiciness,
        Boolean isSoldOut,
        Boolean isActive,
        Integer sort
    ) {
        this.placeId = placeId;
        this.productCategoryId = productCategoryId;
        this.name = name;
        this.description = description;
        this.originalPrice = originalPrice;
        this.discountPrice = discountPrice;
        this.discountRate = discountRate;
        this.rating = rating;
        this.reviewCount = reviewCount != null ? reviewCount : 0;
        this.isRepresentative = isRepresentative != null ? isRepresentative : false;
        this.spiciness = spiciness;
        this.isSoldOut = isSoldOut != null ? isSoldOut : false;
        this.isActive = isActive != null ? isActive : true;
        this.sort = sort;
    }

    public static Product of(
        Long placeId,
        Long productCategoryId,
        String name,
        String description,
        Integer originalPrice,
        Integer discountPrice,
        BigDecimal discountRate,
        Double rating,
        Integer reviewCount,
        Boolean isRepresentative,
        Integer spiciness,
        Boolean isSoldOut,
        Boolean isActive,
        Integer sort
    ) {
        return new Product(
            placeId,
            productCategoryId,
            name,
            description,
            originalPrice,
            discountPrice,
            discountRate,
            rating,
            reviewCount,
            isRepresentative,
            spiciness,
            isSoldOut,
            isActive,
            sort
        );
    }

    public void update(
        Long productCategoryId,
        String name,
        String description,
        Integer originalPrice,
        Integer discountPrice,
        BigDecimal discountRate,
        Boolean isRepresentative,
        Integer spiciness,
        Boolean isSoldOut,
        Boolean isActive,
        Integer sort
    ) {
        this.productCategoryId = productCategoryId;
        this.name = name;
        this.description = description;
        this.originalPrice = originalPrice;
        this.discountPrice = discountPrice;
        this.discountRate = discountRate;
        this.isRepresentative = isRepresentative;
        this.spiciness = spiciness;
        this.isSoldOut = isSoldOut;
        this.isActive = isActive;
        this.sort = sort;
    }
}
