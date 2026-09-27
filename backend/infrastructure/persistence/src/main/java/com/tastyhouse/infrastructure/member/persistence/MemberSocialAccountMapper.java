package com.tastyhouse.infrastructure.member.persistence;

import com.tastyhouse.domain.member.model.MemberSocialAccount;
import com.tastyhouse.domain.member.model.MemberSocialProvider;
import com.tastyhouse.domain.member.vo.MemberId;

final class MemberSocialAccountMapper {
    private MemberSocialAccountMapper() {
    }

    static MemberSocialAccount toDomain(MemberSocialAccountJpaEntity entity) {
        return MemberSocialAccount.reconstitute(
            entity.getId(),
            entity.getMemberId() == null ? null : MemberId.of(entity.getMemberId()),
            entity.getProvider() == null ? null : MemberSocialProvider.valueOf(entity.getProvider()),
            entity.getProviderId(),
            entity.getProviderEmail(),
            entity.getProviderNickname(),
            entity.getProviderProfileImageUrl(),
            entity.getLastLoginAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static MemberSocialAccountJpaEntity toEntity(MemberSocialAccount socialAccount) {
        return MemberSocialAccountJpaEntity.create(
            socialAccount.getMemberId() == null ? null : socialAccount.getMemberId().value(),
            socialAccount.getProvider() == null ? null : socialAccount.getProvider().name(),
            socialAccount.getProviderId(),
            socialAccount.getProviderEmail(),
            socialAccount.getProviderNickname(),
            socialAccount.getProviderProfileImageUrl(),
            socialAccount.getLastLoginAt()
        );
    }

    static void applyChanges(MemberSocialAccountJpaEntity entity, MemberSocialAccount socialAccount) {
        entity.applyChanges(
            socialAccount.getProviderEmail(),
            socialAccount.getProviderNickname(),
            socialAccount.getProviderProfileImageUrl(),
            socialAccount.getLastLoginAt()
        );
    }
}
