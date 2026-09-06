package com.tastyhouse.infrastructure.product.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

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

    static ProductOptionGroupMergeExclusionJpaEntity create(
        Long shopId,
        String groupSignature,
        Long actorCeoId
    ) {
        return new ProductOptionGroupMergeExclusionJpaEntity(shopId, groupSignature, actorCeoId);
    }

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
