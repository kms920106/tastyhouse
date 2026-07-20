package com.tastyhouse.core.domain.product.domain.model;

import java.math.BigDecimal;

import lombok.Getter;

import com.tastyhouse.core.domain.product.domain.vo.ProductDiscountInfo;
import com.tastyhouse.core.domain.product.domain.vo.ProductId;

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
        Integer sort
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
            sort
        );
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자를 주입한다.
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
        Integer sort
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
            sort
        );
    }

    public ProductId getProductId() {
        return ProductId.of(this.id);
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
