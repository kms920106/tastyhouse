package com.tastyhouse.infrastructure.product.persistence;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.domain.product.domain.vo.ProductCategoryId;
import com.tastyhouse.domain.product.domain.vo.ProductDiscountInfo;
import com.tastyhouse.domain.shop.domain.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;
import com.tastyhouse.infrastructure.shop.persistence.ShopIdConverter;

/**
 * 상품 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code Product}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code ProductMapper}가 수행한다.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "PRODUCT")
public class ProductJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = ShopIdConverter.class)
    @Column(name = "shop_id", nullable = false)
    private ShopId shopId;

    @Convert(converter = ProductCategoryIdConverter.class)
    @Column(name = "product_category_id")
    private ProductCategoryId productCategoryId;

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

    @Column(name = "is_visible", nullable = false)
    private boolean visible;

    @Column(name = "sort", nullable = false)
    private Integer sort;

    private ProductJpaEntity(
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
        boolean visible,
        Integer sort
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
        this.visible = visible;
        this.sort = sort;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code ProductMapper#toEntity}에서만 호출한다.
     */
    static ProductJpaEntity create(
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
        boolean visible,
        Integer sort
    ) {
        return new ProductJpaEntity(
            shopId, productCategoryId, name, description, originalPrice, discountInfo,
            rating, reviewCount, representative, spiciness, soldOut, visible, sort
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자·shopId는 건드리지 않는다.
     */
    void applyChanges(
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
        boolean visible,
        Integer sort
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
        this.visible = visible;
        this.sort = sort;
    }
}
