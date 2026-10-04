package com.tastyhouse.infrastructure.persistence.product.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.persistence.shared.persistence.BaseEntity;

@Entity
@Table(name = "PRODUCT_OPTION_GROUP_MERGE_HISTORY")
class ProductOptionGroupMergeHistoryJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "base_option_group_id", nullable = false)
    private Long baseOptionGroupId;

    @Column(name = "merged_option_group_id", nullable = false)
    private Long mergedOptionGroupId;

    @Column(name = "merged_group_name", nullable = false, length = 100)
    private String mergedGroupName;

    @Column(name = "entry_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private String entryType;

    @Column(name = "actor_ceo_id", nullable = false)
    private Long actorCeoId;

    protected ProductOptionGroupMergeHistoryJpaEntity() {
    }

    private ProductOptionGroupMergeHistoryJpaEntity(
        Long shopId,
        Long baseOptionGroupId,
        Long mergedOptionGroupId,
        String mergedGroupName,
        String entryType,
        Long actorCeoId
    ) {
        this.shopId = shopId;
        this.baseOptionGroupId = baseOptionGroupId;
        this.mergedOptionGroupId = mergedOptionGroupId;
        this.mergedGroupName = mergedGroupName;
        this.entryType = entryType;
        this.actorCeoId = actorCeoId;
    }

    static ProductOptionGroupMergeHistoryJpaEntity create(
        Long shopId,
        Long baseOptionGroupId,
        Long mergedOptionGroupId,
        String mergedGroupName,
        String entryType,
        Long actorCeoId
    ) {
        return new ProductOptionGroupMergeHistoryJpaEntity(
            shopId, baseOptionGroupId, mergedOptionGroupId, mergedGroupName, entryType, actorCeoId
        );
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public Long getBaseOptionGroupId() {
        return this.baseOptionGroupId;
    }

    public Long getMergedOptionGroupId() {
        return this.mergedOptionGroupId;
    }

    public String getMergedGroupName() {
        return this.mergedGroupName;
    }

    public String getEntryType() {
        return this.entryType;
    }

    public Long getActorCeoId() {
        return this.actorCeoId;
    }
}
