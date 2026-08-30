package com.tastyhouse.infrastructure.product.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 메뉴 ↔ 공통 옵션그룹 연결 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code ProductCommonOptionGroupLink}와 분리된 영속 전용 엔티티다. 도메인↔엔티티 변환은
 * {@code ProductCommonOptionGroupLinkMapper}가 수행한다.
 *
 * <p>도메인 모델에 감사 필드가 없지만 {@code created_at}·{@code updated_at}이 NOT NULL이므로
 * {@code BaseEntity}를 상속한다 — 매퍼는 이 두 값을 도메인으로 옮기지 않는다.
 */
@Entity
@Table(name = "PRODUCT_COMMON_OPTION_GROUP_LINK")
public class ProductCommonOptionGroupLinkJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "option_group_id", nullable = false)
    private Long optionGroupId;

    @Column(name = "sort", nullable = false)
    private Integer sort;

    protected ProductCommonOptionGroupLinkJpaEntity() {
    }

    private ProductCommonOptionGroupLinkJpaEntity(Long productId, Long optionGroupId, Integer sort) {
        this.productId = productId;
        this.optionGroupId = optionGroupId;
        this.sort = sort;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code ProductCommonOptionGroupLinkMapper#toEntity}에서만 호출한다.
     */
    static ProductCommonOptionGroupLinkJpaEntity create(Long productId, Long optionGroupId, Integer sort) {
        return new ProductCommonOptionGroupLinkJpaEntity(productId, optionGroupId, sort);
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 연결 대상(productId·optionGroupId)은
     * 불변이므로 건드리지 않는다.
     */
    void applyChanges(Integer sort) {
        this.sort = sort;
    }

    public Long getId() {
        return this.id;
    }

    public Long getProductId() {
        return this.productId;
    }

    public Long getOptionGroupId() {
        return this.optionGroupId;
    }

    public Integer getSort() {
        return this.sort;
    }
}
