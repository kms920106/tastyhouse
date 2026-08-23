package com.tastyhouse.infrastructure.product.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 메뉴 ↔ 가게 연결(N:M) JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code ProductShopLink}와 분리된 영속 전용 엔티티다. 변환은
 * {@code ProductShopLinkMapper}가 수행한다.
 *
 * <p>{@code PRODUCT.shop_id}를 대체하지 않는다 — 그 컬럼은 원본 소유 가게로 남고, 이 테이블은
 * "어느 가게 메뉴판에 노출되는가"만 담는다. {@code UNIQUE(product_id, shop_id)}가 같은 메뉴를
 * 같은 가게에 두 번 연결하는 것을 DB 차원에서 막는다.
 *
 * <p>도메인 모델에 감사 필드가 없어도 {@code BaseEntity}를 상속한다 —
 * {@code created_at}·{@code updated_at}이 NOT NULL이라 감사 리스너가 채워야 하기 때문이다
 * ({@code ProductOptionGroupLinkJpaEntity}가 같은 이유로 같은 형태다).
 */
@Entity
@Table(name = "PRODUCT_SHOP_LINK")
public class ProductShopLinkJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "product_category_id")
    private Long productCategoryId; // nullable — 미분류면 null

    @Column(name = "sort", nullable = false)
    private Integer sort;

    protected ProductShopLinkJpaEntity() {
    }

    private ProductShopLinkJpaEntity(Long productId, Long shopId, Long productCategoryId, Integer sort) {
        this.productId = productId;
        this.shopId = shopId;
        this.productCategoryId = productCategoryId;
        this.sort = sort;
    }

    /** 신규 저장용 엔티티를 생성한다(식별자 없음). {@code ProductShopLinkMapper#toEntity}에서만 호출한다. */
    static ProductShopLinkJpaEntity create(Long productId, Long shopId, Long productCategoryId, Integer sort) {
        return new ProductShopLinkJpaEntity(productId, shopId, productCategoryId, sort);
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체).
     * 감사 필드·식별자·불변 필드({@code product_id}·{@code shop_id})는 건드리지 않는다.
     */
    void applyChanges(Long productCategoryId, Integer sort) {
        this.productCategoryId = productCategoryId;
        this.sort = sort;
    }

    public Long getId() {
        return this.id;
    }

    public Long getProductId() {
        return this.productId;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public Long getProductCategoryId() {
        return this.productCategoryId;
    }

    public Integer getSort() {
        return this.sort;
    }
}
