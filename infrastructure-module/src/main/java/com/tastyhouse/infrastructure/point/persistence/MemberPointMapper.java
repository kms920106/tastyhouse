package com.tastyhouse.infrastructure.point.persistence;

import com.tastyhouse.core.domain.point.domain.model.MemberPoint;

/**
 * 회원 포인트 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class MemberPointMapper {

    private MemberPointMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static MemberPoint toDomain(MemberPointJpaEntity entity) {
        return MemberPoint.reconstitute(
            entity.getId(),
            entity.getMemberId(),
            entity.getAvailablePoints(),
            entity.getExpiredThisMonth()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static MemberPointJpaEntity toEntity(MemberPoint domain) {
        return MemberPointJpaEntity.create(
            domain.getMemberId(),
            domain.getAvailablePoints(),
            domain.getExpiredThisMonth()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     */
    static void applyChanges(MemberPointJpaEntity entity, MemberPoint domain) {
        entity.applyChanges(
            domain.getAvailablePoints(),
            domain.getExpiredThisMonth()
        );
    }
}
