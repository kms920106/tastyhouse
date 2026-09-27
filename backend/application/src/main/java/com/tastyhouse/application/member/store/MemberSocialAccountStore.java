package com.tastyhouse.application.member.store;

import java.util.Optional;

import com.tastyhouse.application.member.port.out.write.MemberSocialAccountStatePort;
import com.tastyhouse.domain.member.model.MemberSocialAccount;
import com.tastyhouse.domain.member.model.MemberSocialProvider;

public class MemberSocialAccountStore implements MemberSocialAccountRepository {
    private final MemberSocialAccountStatePort memberSocialAccountStatePort;

    public MemberSocialAccountStore(MemberSocialAccountStatePort memberSocialAccountStatePort) {
        this.memberSocialAccountStatePort = memberSocialAccountStatePort;
    }

    @Override
    public Optional<MemberSocialAccount> findByProviderAndProviderId(MemberSocialProvider provider, String providerId) {
        return memberSocialAccountStatePort.findByProviderAndProviderId(provider == null ? null : provider.name(), providerId)
            .map(MemberSocialAccountStateMapper::toDomain);
    }

    @Override
    public boolean existsByProviderAndProviderId(MemberSocialProvider provider, String providerId) {
        return memberSocialAccountStatePort.existsByProviderAndProviderId(provider == null ? null : provider.name(), providerId);
    }

    @Override
    public MemberSocialAccount save(MemberSocialAccount socialAccount) {
        return MemberSocialAccountStateMapper.toDomain(
            memberSocialAccountStatePort.save(MemberSocialAccountStateMapper.toState(socialAccount))
        );
    }
}
