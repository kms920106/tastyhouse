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
 * 상품 옵션 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code ProductOption}과 분리된 영속 전용 엔티티다. 도메인↔엔티티 변환은
 * {@code ProductOptionMapper}가 수행한다.
 */
@Entity
@Table(name = "PRODUCT_OPTION")
public class ProductOptionJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "option_group_id", nullable = false)
    private Long optionGroupId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "additional_price", nullable = false)
    private Integer additionalPrice;

    @Column(name = "sort", nullable = false)
    private Integer sort;

    @Column(name = "is_sold_out", nullable = false)
    private boolean soldOut;

    @Column(name = "sold_out_until")
    private LocalDateTime soldOutUntil;

    @Column(name = "is_visible", nullable = false)
    private boolean visible;

    /**
     * 일회용컵 제공 개수(1~10). 보증금 옵션그룹의 옵션만 값을 갖는다.
     *
     * <p>보증금 <b>금액</b>이 아니라 <b>개수</b>를 저장하므로 요율(300원)이 바뀌어도 이 컬럼을
     * 마이그레이션할 필요가 없다.
     */
    @Column(name = "cup_count")
    private Integer cupCount;

    /** 개인컵 사용 할인 금액(원). 개인컵 옵션이 아니면 {@code null}이다. 보증금이 아니라 상품 할인 축이다. */
    @Column(name = "personal_cup_discount_amount")
    private Integer personalCupDiscountAmount;

    protected ProductOptionJpaEntity() {
    }

    private ProductOptionJpaEntity(
        Long optionGroupId,
        String name,
        Integer additionalPrice,
        Integer sort,
        boolean soldOut,
        LocalDateTime soldOutUntil,
        boolean visible,
        Integer cupCount,
        Integer personalCupDiscountAmount
    ) {
        this.optionGroupId = optionGroupId;
        this.name = name;
        this.additionalPrice = additionalPrice;
        this.sort = sort;
        this.soldOut = soldOut;
        this.soldOutUntil = soldOutUntil;
        this.visible = visible;
        this.cupCount = cupCount;
        this.personalCupDiscountAmount = personalCupDiscountAmount;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code ProductOptionMapper#toEntity}에서만 호출한다.
     */
    static ProductOptionJpaEntity create(
        Long optionGroupId,
        String name,
        Integer additionalPrice,
        Integer sort,
        boolean soldOut,
        LocalDateTime soldOutUntil,
        boolean visible,
        Integer cupCount,
        Integer personalCupDiscountAmount
    ) {
        return new ProductOptionJpaEntity(
            optionGroupId,
            name,
            additionalPrice,
            sort,
            soldOut,
            soldOutUntil,
            visible,
            cupCount,
            personalCupDiscountAmount
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). optionGroupId는 건드리지 않는다.
     */
    void applyChanges(
        String name,
        Integer additionalPrice,
        Integer sort,
        boolean soldOut,
        LocalDateTime soldOutUntil,
        boolean visible,
        Integer cupCount,
        Integer personalCupDiscountAmount
    ) {
        this.name = name;
        this.additionalPrice = additionalPrice;
        this.sort = sort;
        this.soldOut = soldOut;
        this.soldOutUntil = soldOutUntil;
        this.visible = visible;
        this.cupCount = cupCount;
        this.personalCupDiscountAmount = personalCupDiscountAmount;
    }

    public Long getId() {
        return this.id;
    }

    public Long getOptionGroupId() {
        return this.optionGroupId;
    }

    public String getName() {
        return this.name;
    }

    public Integer getAdditionalPrice() {
        return this.additionalPrice;
    }

    public Integer getSort() {
        return this.sort;
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

    public Integer getCupCount() {
        return this.cupCount;
    }

    public Integer getPersonalCupDiscountAmount() {
        return this.personalCupDiscountAmount;
    }
}
