package com.tastyhouse.domain.product.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductDiscountInfo;
import com.tastyhouse.domain.product.vo.ProductId;

import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public class Product {
    private static final int WEIGHT_TEXT_MAX_LENGTH = 50;

    private final Long id;
    private final ShopId shopId;
    private ProductCategoryId productCategoryId;
    private String name;
    private String description;
    private Integer originalPrice;
    private ProductDiscountInfo discountInfo;
    private Double rating;
    private Integer reviewCount;
    private boolean representative;
    private Integer spiciness;
    private boolean soldOut;

    private LocalDateTime soldOutUntil;
    private boolean visible;
    private Integer sort;

    private boolean ratingExcluded;

    private boolean deleted;

    private String composition;

    private boolean singleServing;

    private LocalDate exposureStartDate;

    private LocalDate exposureEndDate;

    private VegetarianType vegetarianType;

    private String weightText;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private Product(
        Long id,
        ShopId shopId,
        ProductCategoryId productCategoryId,
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
        String weightText,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
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
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Product of(
        ShopId shopId,
        ProductCategoryId productCategoryId,
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
        LocalDateTime soldOutUntil,
        boolean visible,
        Integer sort,
        boolean ratingExcluded,
        String composition,
        boolean singleServing
    ) {
        validatePrices(originalPrice, discountPrice);

        return new Product(
            null,
            shopId,
            productCategoryId,
            name,
            description,
            originalPrice,
            ProductDiscountInfo.of(discountPrice, discountRate),
            rating,
            reviewCount != null ? reviewCount : 0,
            representative,
            spiciness,
            soldOut,
            soldOutUntil,
            visible,
            sort,
            ratingExcluded,
            false,
            composition,
            singleServing,
            null,
            null,
            null,
            null,
            null,
            null
        );
    }

    public static Product reconstitute(
        Long id,
        ShopId shopId,
        ProductCategoryId productCategoryId,
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
        String weightText,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new Product(
            id,
            shopId,
            productCategoryId,
            name,
            description,
            originalPrice,
            discountInfo,
            rating,
            reviewCount,
            representative,
            spiciness,
            soldOut,
            soldOutUntil,
            visible,
            sort,
            ratingExcluded,
            deleted,
            composition,
            singleServing,
            exposureStartDate,
            exposureEndDate,
            vegetarianType,
            weightText,
            createdAt,
            updatedAt
        );
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public ProductCategoryId getProductCategoryId() {
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

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public ProductId getProductId() {
        return ProductId.of(this.id);
    }

    private static void validateWeightText(String weightText) {
        if (weightText != null && weightText.length() > WEIGHT_TEXT_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.PRODUCT_WEIGHT_TEXT_TOO_LONG);
        }
    }

    private static void validatePrices(Integer originalPrice, Integer discountPrice) {
        if (originalPrice != null && originalPrice < 0) {
            throw new BusinessException(ErrorCode.PRODUCT_PRICE_NEGATIVE,
                ErrorCode.PRODUCT_PRICE_NEGATIVE.getDefaultMessage() + " 정가: " + originalPrice);
        }
        if (discountPrice != null && discountPrice < 0) {
            throw new BusinessException(ErrorCode.PRODUCT_PRICE_NEGATIVE,
                ErrorCode.PRODUCT_PRICE_NEGATIVE.getDefaultMessage() + " 할인가: " + discountPrice);
        }
        if (originalPrice != null && discountPrice != null && discountPrice > originalPrice) {
            throw new BusinessException(ErrorCode.PRODUCT_DISCOUNT_PRICE_EXCEEDS_ORIGINAL,
                ErrorCode.PRODUCT_DISCOUNT_PRICE_EXCEEDS_ORIGINAL.getDefaultMessage()
                    + " 정가: " + originalPrice + ", 할인가: " + discountPrice);
        }
    }

    public Integer getDiscountPrice() {
        return discountInfo != null ? discountInfo.discountPrice() : null;
    }

    public BigDecimal getDiscountRate() {
        return discountInfo != null ? discountInfo.discountRate() : null;
    }

    public void syncOriginalPrice(Integer originalPrice) {
        validatePrices(originalPrice, getDiscountPrice());

        this.originalPrice = originalPrice;
    }

    public void updateReviewStats(Double rating, Integer reviewCount) {
        this.rating = rating;
        this.reviewCount = reviewCount;
    }

    public void markSoldOut() {
        this.soldOut = true;
    }

    public void markSoldOut(LocalDateTime soldOutUntil) {
        this.soldOut = true;
        this.soldOutUntil = soldOutUntil;
    }

    public void releaseSoldOut() {
        this.soldOut = false;
        this.soldOutUntil = null;
    }

    public void changeSoldOutUntil(LocalDateTime until) {
        if (!this.soldOut) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_SOLD_OUT);
        }
        this.soldOutUntil = until;
    }

    public void deactivate() {
        this.visible = false;
    }

    public void activate() {
        this.visible = true;
    }

    public void update(
        ProductCategoryId productCategoryId,
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
        validatePrices(originalPrice, discountPrice);

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

        if (!soldOut) {
            this.soldOutUntil = null;
        }
    }

    public void delete() {
        if (this.deleted) {
            throw new BusinessException(ErrorCode.PRODUCT_ALREADY_DELETED);
        }
        this.deleted = true;
        this.visible = false;
    }

    public void relocate(ProductCategoryId productCategoryId, Integer sort) {
        this.productCategoryId = productCategoryId;
        this.sort = sort;
    }

    public void changeSort(Integer sort) {
        this.sort = sort;
    }

    public void changeExposurePeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new BusinessException(ErrorCode.PRODUCT_EXPOSURE_PERIOD_INVALID);
        }
        this.exposureStartDate = startDate;
        this.exposureEndDate = endDate;
    }

    public void applyVegetarianType(VegetarianType type) {
        this.vegetarianType = type;
    }

    public void changeRepresentative(boolean representative) {
        this.representative = representative;
    }

    public void changeDetails(
        ProductCategoryId productCategoryId,
        String name,
        String composition,
        String description,
        Integer originalPrice,
        Integer discountPrice,
        BigDecimal discountRate,
        boolean singleServing,
        Integer spiciness,
        boolean representative,
        boolean ratingExcluded,
        String weightText
    ) {
        validatePrices(originalPrice, discountPrice);
        validateWeightText(weightText);

        this.productCategoryId = productCategoryId;
        this.name = name;
        this.composition = composition;
        this.description = description;
        this.originalPrice = originalPrice;
        this.discountInfo = ProductDiscountInfo.of(discountPrice, discountRate);
        this.singleServing = singleServing;
        this.spiciness = spiciness;
        this.representative = representative;
        this.ratingExcluded = ratingExcluded;
        this.weightText = weightText;
    }
}
