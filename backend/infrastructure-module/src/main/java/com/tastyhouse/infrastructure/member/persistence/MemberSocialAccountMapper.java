package com.tastyhouse.infrastructure.member.persistence;

import com.tastyhouse.domain.member.model.MemberSocialAccount;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 회원 소셜 계정 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class MemberSocialAccountMapper {

    private MemberSocialAccountMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static MemberSocialAccount toDomain(MemberSocialAccountJpaEntity entity) {
        return MemberSocialAccount.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getMemberId(), MemberId::of),
            entity.getProvider(),
            entity.getProviderId(),
            entity.getProviderEmail(),
            entity.getProviderNickname(),
            entity.getProviderProfileImageUrl(),
            entity.getLastLoginAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static MemberSocialAccountJpaEntity toEntity(MemberSocialAccount domain) {
        return MemberSocialAccountJpaEntity.create(
            IdMapping.raw(domain.getMemberId(), MemberId::value),
            domain.getProvider(),
            domain.getProviderId(),
            domain.getProviderEmail(),
            domain.getProviderNickname(),
            domain.getProviderProfileImageUrl(),
            domain.getLastLoginAt()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     */
    static void applyChanges(MemberSocialAccountJpaEntity entity, MemberSocialAccount domain) {
        entity.applyChanges(
            domain.getProviderEmail(),
            domain.getProviderNickname(),
            domain.getProviderProfileImageUrl(),
            domain.getLastLoginAt()
        );
    }
}
