package com.tastyhouse.infrastructure.member.referral.persistence;

import com.tastyhouse.domain.member.referral.domain.model.MemberReferral;

/**
 * 회원 추천 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class MemberReferralMapper {

    private MemberReferralMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static MemberReferral toDomain(MemberReferralJpaEntity entity) {
        return MemberReferral.reconstitute(
            entity.getId(),
            entity.getReferrerId(),
            entity.getRefereeId(),
            entity.getStatus(),
            entity.getCreatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static MemberReferralJpaEntity toEntity(MemberReferral domain) {
        return MemberReferralJpaEntity.create(
            domain.getReferrerId(),
            domain.getRefereeId(),
            domain.getStatus()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     */
    static void applyChanges(MemberReferralJpaEntity entity, MemberReferral domain) {
        entity.applyChanges(domain.getStatus());
    }
}
