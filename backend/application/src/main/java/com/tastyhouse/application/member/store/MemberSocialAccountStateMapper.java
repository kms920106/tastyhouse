package com.tastyhouse.application.member.store;

import com.tastyhouse.domain.member.model.MemberSocialAccount;
import com.tastyhouse.domain.member.model.MemberSocialProvider;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.member.port.out.write.MemberSocialAccountState;

final class MemberSocialAccountStateMapper {
    private MemberSocialAccountStateMapper() {
    }

    static MemberSocialAccount toDomain(MemberSocialAccountState state) {
        return MemberSocialAccount.reconstitute(
            state.id(),
            state.memberId() == null ? null : MemberId.of(state.memberId()),
            state.provider() == null ? null : MemberSocialProvider.valueOf(state.provider()),
            state.providerId(),
            state.providerEmail(),
            state.providerNickname(),
            state.providerProfileImageUrl(),
            state.lastLoginAt(),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static MemberSocialAccountState toState(MemberSocialAccount socialAccount) {
        return new MemberSocialAccountState(
            socialAccount.getId(),
            socialAccount.getMemberId() == null ? null : socialAccount.getMemberId().value(),
            socialAccount.getProvider() == null ? null : socialAccount.getProvider().name(),
            socialAccount.getProviderId(),
            socialAccount.getProviderEmail(),
            socialAccount.getProviderNickname(),
            socialAccount.getProviderProfileImageUrl(),
            socialAccount.getLastLoginAt(),
            socialAccount.getCreatedAt(),
            socialAccount.getUpdatedAt()
        );
    }
}
