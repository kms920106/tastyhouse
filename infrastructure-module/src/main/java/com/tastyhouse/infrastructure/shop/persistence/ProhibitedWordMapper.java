package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.domain.model.ProhibitedWord;

/**
 * 금칙어 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 * 읽기 전용 애그리거트라 {@code toDomain}만 필요하다.
 */
final class ProhibitedWordMapper {

    private ProhibitedWordMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static ProhibitedWord toDomain(ProhibitedWordJpaEntity entity) {
        return ProhibitedWord.reconstitute(
            entity.getId(),
            entity.getWord(),
            entity.getReason()
        );
    }
}
