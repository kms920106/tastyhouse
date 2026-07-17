package com.tastyhouse.core.domain.member.infrastructure.persistence;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.member.domain.model.MemberSocialAccount;
import com.tastyhouse.core.domain.member.domain.model.MemberSocialProvider;
import com.tastyhouse.core.domain.member.domain.repository.MemberSocialAccountRepository;

@Repository
@RequiredArgsConstructor
public class MemberSocialAccountRepositoryImpl implements MemberSocialAccountRepository {

    private final MemberSocialAccountJpaRepository memberSocialAccountJpaRepository;

    @Override
    public Optional<MemberSocialAccount> findByProviderAndProviderId(MemberSocialProvider provider, String providerId) {
        return memberSocialAccountJpaRepository.findByProviderAndProviderId(provider, providerId);
    }

    @Override
    public boolean existsByProviderAndProviderId(MemberSocialProvider provider, String providerId) {
        return memberSocialAccountJpaRepository.existsByProviderAndProviderId(provider, providerId);
    }

    @Override
    public MemberSocialAccount save(MemberSocialAccount socialAccount) {
        return memberSocialAccountJpaRepository.save(socialAccount);
    }
}
