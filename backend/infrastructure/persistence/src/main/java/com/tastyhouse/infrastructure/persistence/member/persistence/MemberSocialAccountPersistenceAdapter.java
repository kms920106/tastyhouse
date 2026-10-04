package com.tastyhouse.infrastructure.persistence.member.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.member.model.MemberSocialAccount;
import com.tastyhouse.domain.member.model.MemberSocialProvider;
import com.tastyhouse.application.member.port.out.write.MemberSocialAccountPersistencePort;

@Repository
class MemberSocialAccountPersistenceAdapter implements MemberSocialAccountPersistencePort {

    private final MemberSocialAccountJpaRepository memberSocialAccountJpaRepository;

    public MemberSocialAccountPersistenceAdapter(MemberSocialAccountJpaRepository memberSocialAccountJpaRepository) {
        this.memberSocialAccountJpaRepository = memberSocialAccountJpaRepository;
    }

    @Override
    public Optional<MemberSocialAccount> findByProviderAndProviderId(MemberSocialProvider provider, String providerId) {
        return memberSocialAccountJpaRepository.findByProviderAndProviderId(provider == null ? null : provider.name(), providerId)
            .map(MemberSocialAccountMapper::toDomain);
    }

    @Override
    public boolean existsByProviderAndProviderId(MemberSocialProvider provider, String providerId) {
        return memberSocialAccountJpaRepository.existsByProviderAndProviderId(provider == null ? null : provider.name(), providerId);
    }

    @Override
    public MemberSocialAccount save(MemberSocialAccount socialAccount) {
        if (socialAccount.getId() == null) {
            MemberSocialAccountJpaEntity saved = memberSocialAccountJpaRepository.save(MemberSocialAccountMapper.toEntity(socialAccount));
            return MemberSocialAccountMapper.toDomain(saved);
        }

        MemberSocialAccountJpaEntity entity = memberSocialAccountJpaRepository.findById(socialAccount.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 소셜 계정입니다: " + socialAccount.getId()));
        MemberSocialAccountMapper.applyChanges(entity, socialAccount);
        return MemberSocialAccountMapper.toDomain(entity);
    }
}
