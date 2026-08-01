package com.tastyhouse.domain.product.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Getter;

import com.tastyhouse.domain.product.domain.vo.ProductDiscountInfo;
import com.tastyhouse.domain.product.domain.vo.ProductId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 상품 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ProductJpaEntity} + {@code ProductMapper}가 담당한다. 도메인이 프레임워크-프리이므로
 * 변경 후 저장은 더티 체킹이 아니라 command 서비스가 명시적으로 {@code ProductRepository#save}를
 * 호출해야 한다.
 */
@Getter
public class Product {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final Long shopId;
    private Long productCategoryId;
    private String name;
    private String description;
    private Integer originalPrice;
    private ProductDiscountInfo discountInfo;
    private Double rating;
    private Integer reviewCount;
    private boolean representative;
    private Integer spiciness;
    private boolean soldOut;
    private boolean visible;
    private Integer sort;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private Product(
        Long id,
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
        boolean visible,
        Integer sort,
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
        this.visible = visible;
        this.sort = sort;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 신규 상품을 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다.
     *
     * <p>가격 불변식({@link #validatePrices})을 강제한다 — 정가·할인가 음수 금지, 할인가 &lt;= 정가.
     *
     * <p>{@link #reconstitute}는 이 검증을 <b>거치지 않는다</b> — 기존 DB 데이터가 새 불변식을 위반해도
     * 로드는 가능해야 하기 때문이다.
     */
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
            visible,
            sort,
            null,
            null
        );
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자를 주입한다.
     *
     * <p><b>{@link #of}와 달리 가격 불변식 검증을 하지 않는다</b> — 불변식 도입 이전에 저장된 기존 상품이
     * 새 규칙을 위반하더라도 로드는 가능해야 하기 때문이다.
     */
    public static Product reconstitute(
        Long id,
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
        boolean visible,
        Integer sort,
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
            visible,
            sort,
            createdAt,
            updatedAt
        );
    }

    public ProductId getProductId() {
        return ProductId.of(this.id);
    }

    /**
     * 가격 불변식을 검증한다 — 신규 생성({@code of})과 변경({@code update}) 양쪽이 같은 검증 한 벌을
     * 공유한다. 생성만 막고 변경을 열어두면 같은 위반 값이 곧바로 뒷문으로 들어오기 때문이다.
     *
     * <p>검증 항목: {@code originalPrice} 음수 금지, {@code discountPrice} 음수 금지,
     * {@code discountPrice <= originalPrice}. {@code discountPrice}가 null이면 "할인 없음"이므로
     * 비교 대상에서 제외한다.
     *
     * <p>{@code originalPrice}가 null인 경우는 여기서 막지 않는다 — 기존 호출부가 필수값으로 보장하며
     * (HTTP 경계 {@code @NotNull}), 이 태스크의 범위는 "음수·역전 금지"다.
     */
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
    }
}
