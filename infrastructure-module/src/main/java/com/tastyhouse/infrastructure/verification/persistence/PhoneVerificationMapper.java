package com.tastyhouse.infrastructure.verification.persistence;

import com.tastyhouse.core.domain.verification.domain.model.PhoneVerification;

/**
 * 휴대폰 인증 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class PhoneVerificationMapper {

    private PhoneVerificationMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static PhoneVerification toDomain(PhoneVerificationJpaEntity entity) {
        return PhoneVerification.reconstitute(
            entity.getId(),
            entity.getPhoneNumber(),
            entity.getVerificationCode(),
            entity.getStatus(),
            entity.getExpiresAt(),
            entity.getVerifiedAt(),
            entity.getCreatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static PhoneVerificationJpaEntity toEntity(PhoneVerification domain) {
        return PhoneVerificationJpaEntity.create(
            domain.getPhoneNumber(),
            domain.getVerificationCode(),
            domain.getStatus(),
            domain.getExpiresAt(),
            domain.getVerifiedAt(),
            domain.getCreatedAt()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     */
    static void applyChanges(PhoneVerificationJpaEntity entity, PhoneVerification domain) {
        entity.applyChanges(domain.getStatus(), domain.getVerifiedAt());
    }
}
