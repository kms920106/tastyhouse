package com.tastyhouse.infrastructure.product.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.core.shared.entity.BaseEntity;

/**
 * 상품 옵션 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code ProductOption}과 분리된 영속 전용 엔티티다. 도메인↔엔티티 변환은
 * {@code ProductOptionMapper}가 수행한다.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
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

    @Column(name = "is_visible", nullable = false)
    private boolean visible;

    private ProductOptionJpaEntity(
        Long optionGroupId,
        String name,
        Integer additionalPrice,
        Integer sort,
        boolean soldOut,
        boolean visible
    ) {
        this.optionGroupId = optionGroupId;
        this.name = name;
        this.additionalPrice = additionalPrice;
        this.sort = sort;
        this.soldOut = soldOut;
        this.visible = visible;
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
        boolean visible
    ) {
        return new ProductOptionJpaEntity(optionGroupId, name, additionalPrice, sort, soldOut, visible);
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). optionGroupId는 건드리지 않는다.
     */
    void applyChanges(String name, Integer additionalPrice, Integer sort, boolean soldOut, boolean visible) {
        this.name = name;
        this.additionalPrice = additionalPrice;
        this.sort = sort;
        this.soldOut = soldOut;
        this.visible = visible;
    }
}
