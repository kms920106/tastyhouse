package com.tastyhouse.infrastructure.member.persistence;

import com.tastyhouse.domain.member.model.MemberSocialAccount;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class MemberSocialAccountMapper {
    private MemberSocialAccountMapper() {
    }

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

    static void applyChanges(MemberSocialAccountJpaEntity entity, MemberSocialAccount domain) {
        entity.applyChanges(
            domain.getProviderEmail(),
            domain.getProviderNickname(),
            domain.getProviderProfileImageUrl(),
            domain.getLastLoginAt()
        );
    }
}
