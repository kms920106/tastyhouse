package com.tastyhouse.infrastructure.product.persistence;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.product.model.VegetarianType;
import com.tastyhouse.domain.product.vo.ProductDiscountInfo;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "PRODUCT")
public class ProductJpaEntity extends BaseEntity {
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
    @AttributeOverrides({
        @AttributeOverride(name = "discountPrice", column = @Column(name = "discount_price")),
        @AttributeOverride(name = "discountRate", column = @Column(name = "discount_rate"))
    })
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

    @Column(name = "sold_out_until")
    private LocalDateTime soldOutUntil;

    @Column(name = "is_visible", nullable = false)
    private boolean visible;

    @Column(name = "sort", nullable = false)
    private Integer sort;

    @Column(name = "is_rating_excluded", nullable = false)
    private boolean ratingExcluded;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    @Column(name = "composition", length = 500)
    private String composition;

    @Column(name = "single_serving", nullable = false)
    private boolean singleServing;

    @Column(name = "exposure_start_date")
    private LocalDate exposureStartDate;

    @Column(name = "exposure_end_date")
    private LocalDate exposureEndDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "vegetarian_type", length = 20, columnDefinition = "VARCHAR(20)")
    private VegetarianType vegetarianType;

    @Column(name = "weight_text", length = 50)
    private String weightText;

    protected ProductJpaEntity() {
    }

    private ProductJpaEntity(
        Long shopId,
        Long productCategoryId,
        String name,
        String description,
        Integer originalPrice,
        ProductDiscountInfo discountInfo,
        Double rating,
        Integer reviewCount,
        boolean representative,
        Integer spiciness,
        boolean soldOut,
        LocalDateTime soldOutUntil,
        boolean visible,
        Integer sort,
        boolean ratingExcluded,
        boolean deleted,
        String composition,
        boolean singleServing,
        LocalDate exposureStartDate,
        LocalDate exposureEndDate,
        VegetarianType vegetarianType,
        String weightText
    ) {
        this.shopId = shopId;
        this.productCategoryId = productCategoryId;
        this.name = name;
        this.description = description;
        this.originalPrice = originalPrice;
        this.discountInfo = discountInfo;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.representative = representative;
        this.spiciness = spiciness;
        this.soldOut = soldOut;
        this.soldOutUntil = soldOutUntil;
        this.visible = visible;
        this.sort = sort;
        this.ratingExcluded = ratingExcluded;
        this.deleted = deleted;
        this.composition = composition;
        this.singleServing = singleServing;
        this.exposureStartDate = exposureStartDate;
        this.exposureEndDate = exposureEndDate;
        this.vegetarianType = vegetarianType;
        this.weightText = weightText;
    }

    static ProductJpaEntity create(
        Long shopId,
        Long productCategoryId,
        String name,
        String description,
        Integer originalPrice,
        ProductDiscountInfo discountInfo,
        Double rating,
        Integer reviewCount,
        boolean representative,
        Integer spiciness,
        boolean soldOut,
        LocalDateTime soldOutUntil,
        boolean visible,
        Integer sort,
        boolean ratingExcluded,
        boolean deleted,
        String composition,
        boolean singleServing,
        LocalDate exposureStartDate,
        LocalDate exposureEndDate,
        VegetarianType vegetarianType,
        String weightText
    ) {
        return new ProductJpaEntity(
            shopId, productCategoryId, name, description, originalPrice, discountInfo,
            rating, reviewCount, representative, spiciness, soldOut, soldOutUntil, visible, sort, ratingExcluded,
            deleted, composition, singleServing, exposureStartDate, exposureEndDate, vegetarianType, weightText
        );
    }

    void applyChanges(
        Long productCategoryId,
        String name,
        String description,
        Integer originalPrice,
        ProductDiscountInfo discountInfo,
        Double rating,
        Integer reviewCount,
        boolean representative,
        Integer spiciness,
        boolean soldOut,
        LocalDateTime soldOutUntil,
        boolean visible,
        Integer sort,
        boolean ratingExcluded,
        boolean deleted,
        String composition,
        boolean singleServing,
        LocalDate exposureStartDate,
        LocalDate exposureEndDate,
        VegetarianType vegetarianType,
        String weightText
    ) {
        this.productCategoryId = productCategoryId;
        this.name = name;
        this.description = description;
        this.originalPrice = originalPrice;
        this.discountInfo = discountInfo;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.representative = representative;
        this.spiciness = spiciness;
        this.soldOut = soldOut;
        this.soldOutUntil = soldOutUntil;
        this.visible = visible;
        this.sort = sort;
        this.ratingExcluded = ratingExcluded;
        this.deleted = deleted;
        this.composition = composition;
        this.singleServing = singleServing;
        this.exposureStartDate = exposureStartDate;
        this.exposureEndDate = exposureEndDate;
        this.vegetarianType = vegetarianType;
        this.weightText = weightText;
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public Long getProductCategoryId() {
        return this.productCategoryId;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public Integer getOriginalPrice() {
        return this.originalPrice;
    }

    public ProductDiscountInfo getDiscountInfo() {
        return this.discountInfo;
    }

    public Double getRating() {
        return this.rating;
    }

    public Integer getReviewCount() {
        return this.reviewCount;
    }

    public boolean isRepresentative() {
        return this.representative;
    }

    public Integer getSpiciness() {
        return this.spiciness;
    }

    public boolean isSoldOut() {
        return this.soldOut;
    }

    public LocalDateTime getSoldOutUntil() {
        return this.soldOutUntil;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public Integer getSort() {
        return this.sort;
    }

    public boolean isRatingExcluded() {
        return this.ratingExcluded;
    }

    public boolean isDeleted() {
        return this.deleted;
    }

    public String getComposition() {
        return this.composition;
    }

    public boolean isSingleServing() {
        return this.singleServing;
    }

    public LocalDate getExposureStartDate() {
        return this.exposureStartDate;
    }

    public LocalDate getExposureEndDate() {
        return this.exposureEndDate;
    }

    public VegetarianType getVegetarianType() {
        return this.vegetarianType;
    }

    public String getWeightText() {
        return this.weightText;
    }
}
