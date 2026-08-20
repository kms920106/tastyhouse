package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.product.model.ProductExposureHour;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 메뉴 노출 요일·시간대 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class ProductExposureHourMapper {

    private ProductExposureHourMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로). 도메인 모델에 감사 필드가 없어 생성·수정 일시는 옮기지 않는다.
     */
    static ProductExposureHour toDomain(ProductExposureHourJpaEntity entity) {
        return ProductExposureHour.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getProductId(), ProductId::of),
            entity.getDayType(),
            entity.getStartTime(),
            entity.getEndTime()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static ProductExposureHourJpaEntity toEntity(ProductExposureHour domain) {
        return ProductExposureHourJpaEntity.create(
            IdMapping.raw(domain.getProductId(), ProductId::value),
            domain.getDayType(),
            domain.getStartTime(),
            domain.getEndTime()
        );
    }
}
