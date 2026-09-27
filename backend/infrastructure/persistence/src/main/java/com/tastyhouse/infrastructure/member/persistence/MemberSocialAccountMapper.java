package com.tastyhouse.infrastructure.member.persistence;

import com.tastyhouse.application.member.port.out.write.MemberSocialAccountState;

final class MemberSocialAccountMapper {
    private MemberSocialAccountMapper() {
    }

    static MemberSocialAccountState toState(MemberSocialAccountJpaEntity entity) {
        return new MemberSocialAccountState(
            entity.getId(),
            entity.getMemberId(),
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

    static MemberSocialAccountJpaEntity toEntity(MemberSocialAccountState state) {
        return MemberSocialAccountJpaEntity.create(
            state.memberId(),
            state.provider(),
            state.providerId(),
            state.providerEmail(),
            state.providerNickname(),
            state.providerProfileImageUrl(),
            state.lastLoginAt()
        );
    }

    static void applyChanges(MemberSocialAccountJpaEntity entity, MemberSocialAccountState state) {
        entity.applyChanges(
            state.providerEmail(),
            state.providerNickname(),
            state.providerProfileImageUrl(),
            state.lastLoginAt()
        );
    }
}
