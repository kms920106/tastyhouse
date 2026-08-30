package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.product.model.ProductOptionGroupMergeHistory;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/** 옵션그룹 합치기 이력 도메인 모델 ↔ JPA 엔티티 변환기. */
final class ProductOptionGroupMergeHistoryMapper {

    private ProductOptionGroupMergeHistoryMapper() {
    }

    /** JPA 엔티티를 도메인 모델로 재구성한다. 도메인에 감사 필드가 없어 생성·수정 일시는 옮기지 않는다. */
    static ProductOptionGroupMergeHistory toDomain(ProductOptionGroupMergeHistoryJpaEntity entity) {
        return ProductOptionGroupMergeHistory.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            IdMapping.vo(entity.getBaseOptionGroupId(), ProductOptionGroupId::of),
            IdMapping.vo(entity.getMergedOptionGroupId(), ProductOptionGroupId::of),
            entity.getMergedGroupName(),
            entity.getEntryType(),
            IdMapping.vo(entity.getActorCeoId(), CeoId::of)
        );
    }

    /** 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태). */
    static ProductOptionGroupMergeHistoryJpaEntity toEntity(ProductOptionGroupMergeHistory domain) {
        return ProductOptionGroupMergeHistoryJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            IdMapping.raw(domain.getBaseOptionGroupId(), ProductOptionGroupId::value),
            IdMapping.raw(domain.getMergedOptionGroupId(), ProductOptionGroupId::value),
            domain.getMergedGroupName(),
            domain.getEntryType(),
            IdMapping.raw(domain.getActorCeoId(), CeoId::value)
        );
    }
}
