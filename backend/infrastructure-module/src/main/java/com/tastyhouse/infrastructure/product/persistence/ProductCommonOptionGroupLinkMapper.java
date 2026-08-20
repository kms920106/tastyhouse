package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.product.model.ProductCommonOptionGroupLink;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 메뉴 ↔ 공통 옵션그룹 연결 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class ProductCommonOptionGroupLinkMapper {

    private ProductCommonOptionGroupLinkMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로). 도메인 모델에 감사 필드가 없어 생성·수정 일시는 옮기지 않는다.
     */
    static ProductCommonOptionGroupLink toDomain(ProductCommonOptionGroupLinkJpaEntity entity) {
        return ProductCommonOptionGroupLink.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getProductId(), ProductId::of),
            IdMapping.vo(entity.getOptionGroupId(), ProductOptionGroupId::of),
            entity.getSort()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static ProductCommonOptionGroupLinkJpaEntity toEntity(ProductCommonOptionGroupLink domain) {
        return ProductCommonOptionGroupLinkJpaEntity.create(
            IdMapping.raw(domain.getProductId(), ProductId::value),
            IdMapping.raw(domain.getOptionGroupId(), ProductOptionGroupId::value),
            domain.getSort()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     */
    static void applyChanges(ProductCommonOptionGroupLinkJpaEntity entity, ProductCommonOptionGroupLink domain) {
        entity.applyChanges(domain.getSort());
    }
}
