package com.tastyhouse.infrastructure.product.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 옵션그룹 합치기 추천 제외 JPA 영속 모델(append-only).
 *
 * <p>{@code group_signature}는 SHA-256 hex 64자 고정이라 {@code CHAR(64)}다.
 * {@code UNIQUE (shop_id, group_signature)}가 재클릭 멱등성을 물리적으로 보장한다.
 */
@Entity
@Table(name = "PRODUCT_OPTION_GROUP_MERGE_EXCLUSION")
public class ProductOptionGroupMergeExclusionJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "group_signature", nullable = false, length = 64, columnDefinition = "CHAR(64)")
    private String groupSignature;

    @Column(name = "actor_ceo_id", nullable = false)
    private Long actorCeoId;

    protected ProductOptionGroupMergeExclusionJpaEntity() {
    }

    private ProductOptionGroupMergeExclusionJpaEntity(Long shopId, String groupSignature, Long actorCeoId) {
        this.shopId = shopId;
        this.groupSignature = groupSignature;
        this.actorCeoId = actorCeoId;
    }

    /** 신규 저장용 엔티티를 생성한다(식별자 없음). 매퍼에서만 호출한다. */
    static ProductOptionGroupMergeExclusionJpaEntity create(
        Long shopId,
        String groupSignature,
        Long actorCeoId
    ) {
        return new ProductOptionGroupMergeExclusionJpaEntity(shopId, groupSignature, actorCeoId);
    }

    // append-only라 applyChanges를 두지 않는다.

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public String getGroupSignature() {
        return this.groupSignature;
    }

    public Long getActorCeoId() {
        return this.actorCeoId;
    }
}
