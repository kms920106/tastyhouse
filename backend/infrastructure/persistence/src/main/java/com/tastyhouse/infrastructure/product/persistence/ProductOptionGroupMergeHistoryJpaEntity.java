package com.tastyhouse.infrastructure.product.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.product.model.ProductOptionGroupMergeEntryType;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 옵션그룹 합치기 이력 JPA 영속 모델(append-only).
 *
 * <p>{@code entryType}은 {@code @Enumerated(STRING)} + {@code columnDefinition = "VARCHAR(20)"}로
 * 매핑한다 — Hibernate 6의 {@code MySQLDialect}는 STRING enum을 네이티브 {@code ENUM(...)}으로
 * 기대하므로, {@code columnDefinition}을 빼면 {@code ddl-auto: validate}가 부팅을 거부한다.
 */
@Entity
@Table(name = "PRODUCT_OPTION_GROUP_MERGE_HISTORY")
public class ProductOptionGroupMergeHistoryJpaEntity extends BaseEntity {

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

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private ProductOptionGroupMergeEntryType entryType;

    @Column(name = "actor_ceo_id", nullable = false)
    private Long actorCeoId;

    protected ProductOptionGroupMergeHistoryJpaEntity() {
    }

    private ProductOptionGroupMergeHistoryJpaEntity(
        Long shopId,
        Long baseOptionGroupId,
        Long mergedOptionGroupId,
        String mergedGroupName,
        ProductOptionGroupMergeEntryType entryType,
        Long actorCeoId
    ) {
        this.shopId = shopId;
        this.baseOptionGroupId = baseOptionGroupId;
        this.mergedOptionGroupId = mergedOptionGroupId;
        this.mergedGroupName = mergedGroupName;
        this.entryType = entryType;
        this.actorCeoId = actorCeoId;
    }

    /** 신규 저장용 엔티티를 생성한다(식별자 없음). 매퍼에서만 호출한다. */
    static ProductOptionGroupMergeHistoryJpaEntity create(
        Long shopId,
        Long baseOptionGroupId,
        Long mergedOptionGroupId,
        String mergedGroupName,
        ProductOptionGroupMergeEntryType entryType,
        Long actorCeoId
    ) {
        return new ProductOptionGroupMergeHistoryJpaEntity(
            shopId, baseOptionGroupId, mergedOptionGroupId, mergedGroupName, entryType, actorCeoId
        );
    }

    // append-only라 applyChanges를 두지 않는다 — 이력은 기록된 뒤 바뀌지 않는다.

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

    public ProductOptionGroupMergeEntryType getEntryType() {
        return this.entryType;
    }

    public Long getActorCeoId() {
        return this.actorCeoId;
    }
}
