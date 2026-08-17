package com.tastyhouse.infrastructure.product.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.product.vo.ProductDiscountInfo;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 상품 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code Product}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code ProductMapper}가 수행한다.
 */
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

    /**
     * 메뉴 평가 제외 여부(주류·사이드 등). 도메인 모델에서 {@code final}이라 {@link #applyChanges}의 복사
     * 대상이 아니다 — 여기에 추가하면 "상품 수정으로 바꿀 수 있다"는 잘못된 신호가 된다.
     */
    @Column(name = "is_rating_excluded", nullable = false)
    private boolean ratingExcluded;

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
        boolean ratingExcluded
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
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code ProductMapper#toEntity}에서만 호출한다.
     */
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
        boolean ratingExcluded
    ) {
        return new ProductJpaEntity(
            shopId, productCategoryId, name, description, originalPrice, discountInfo,
            rating, reviewCount, representative, spiciness, soldOut, soldOutUntil, visible, sort, ratingExcluded
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자·shopId는 건드리지 않는다.
     */
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
        this.soldOutUntil = soldOutUntil;
        this.visible = visible;
        this.sort = sort;
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
}
