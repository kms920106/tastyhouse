package com.tastyhouse.infrastructure.product.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 메뉴 가격(가격명 + 채널별 가격 세 벌) JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code ProductPrice}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사
 * 필드)만 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code ProductPriceMapper}가
 * 수행한다.
 *
 * <p>{@code pickup_price_set_at}을 별도 컬럼으로 들고 있는 이유는 '매장가격 픽업' 뱃지가 <b>픽업가
 * 설정 익일(영업일)</b>부터 노출되기 때문이다 — 감사 필드 {@code updated_at}으로 대체할 수 없다
 * (가격명·정렬만 바뀌어도 갱신되므로 뱃지 노출 시점이 뒤로 밀린다).
 */
@Entity
@Table(name = "PRODUCT_PRICE")
public class ProductPriceJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "price_name", length = 50)
    private String priceName; // nullable — null이면 단일 가격(가격명 없음)

    @Column(name = "delivery_price", nullable = false)
    private Integer deliveryPrice;

    @Column(name = "store_price")
    private Integer storePrice; // nullable — 매장 가격 인증 승인 후에만 설정

    @Column(name = "pickup_price")
    private Integer pickupPrice; // nullable — 인증 후에만 설정. 미설정이면 배달가를 쓴다

    @Column(name = "sort", nullable = false)
    private Integer sort;

    @Column(name = "pickup_price_set_at")
    private LocalDateTime pickupPriceSetAt; // nullable — 픽업가 미설정이면 null

    protected ProductPriceJpaEntity() {
    }

    private ProductPriceJpaEntity(
        Long productId,
        String priceName,
        Integer deliveryPrice,
        Integer storePrice,
        Integer pickupPrice,
        Integer sort,
        LocalDateTime pickupPriceSetAt
    ) {
        this.productId = productId;
        this.priceName = priceName;
        this.deliveryPrice = deliveryPrice;
        this.storePrice = storePrice;
        this.pickupPrice = pickupPrice;
        this.sort = sort;
        this.pickupPriceSetAt = pickupPriceSetAt;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code ProductPriceMapper#toEntity}에서만 호출한다.
     */
    static ProductPriceJpaEntity create(
        Long productId,
        String priceName,
        Integer deliveryPrice,
        Integer storePrice,
        Integer pickupPrice,
        Integer sort,
        LocalDateTime pickupPriceSetAt
    ) {
        return new ProductPriceJpaEntity(
            productId,
            priceName,
            deliveryPrice,
            storePrice,
            pickupPrice,
            sort,
            pickupPriceSetAt
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자·
     * 불변 필드({@code product_id})는 건드리지 않는다.
     */
    void applyChanges(
        String priceName,
        Integer deliveryPrice,
        Integer storePrice,
        Integer pickupPrice,
        Integer sort,
        LocalDateTime pickupPriceSetAt
    ) {
        this.priceName = priceName;
        this.deliveryPrice = deliveryPrice;
        this.storePrice = storePrice;
        this.pickupPrice = pickupPrice;
        this.sort = sort;
        this.pickupPriceSetAt = pickupPriceSetAt;
    }

    public Long getId() {
        return this.id;
    }

    public Long getProductId() {
        return this.productId;
    }

    public String getPriceName() {
        return this.priceName;
    }

    public Integer getDeliveryPrice() {
        return this.deliveryPrice;
    }

    public Integer getStorePrice() {
        return this.storePrice;
    }

    public Integer getPickupPrice() {
        return this.pickupPrice;
    }

    public Integer getSort() {
        return this.sort;
    }

    public LocalDateTime getPickupPriceSetAt() {
        return this.pickupPriceSetAt;
    }
}
