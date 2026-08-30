package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.product.model.ProductOptionGroupMergeExclusion;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/** 옵션그룹 합치기 추천 제외 도메인 모델 ↔ JPA 엔티티 변환기. */
final class ProductOptionGroupMergeExclusionMapper {

    private ProductOptionGroupMergeExclusionMapper() {
    }

    /** JPA 엔티티를 도메인 모델로 재구성한다. 도메인에 감사 필드가 없어 생성·수정 일시는 옮기지 않는다. */
    static ProductOptionGroupMergeExclusion toDomain(ProductOptionGroupMergeExclusionJpaEntity entity) {
        return ProductOptionGroupMergeExclusion.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            entity.getGroupSignature(),
            IdMapping.vo(entity.getActorCeoId(), CeoId::of)
        );
    }

    /** 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태). */
    static ProductOptionGroupMergeExclusionJpaEntity toEntity(ProductOptionGroupMergeExclusion domain) {
        return ProductOptionGroupMergeExclusionJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            domain.getGroupSignature(),
            IdMapping.raw(domain.getActorCeoId(), CeoId::value)
        );
    }
}
