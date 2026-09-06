package com.tastyhouse.infrastructure.member.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.member.model.MemberSocialAccount;
import com.tastyhouse.domain.member.model.MemberSocialProvider;
import com.tastyhouse.domain.member.repository.MemberSocialAccountRepository;

@Repository
public class MemberSocialAccountRepositoryImpl implements MemberSocialAccountRepository {
    private final MemberSocialAccountJpaRepository memberSocialAccountJpaRepository;

    public MemberSocialAccountRepositoryImpl(MemberSocialAccountJpaRepository memberSocialAccountJpaRepository) {
        this.memberSocialAccountJpaRepository = memberSocialAccountJpaRepository;
    }

    @Override
    public Optional<MemberSocialAccount> findByProviderAndProviderId(MemberSocialProvider provider, String providerId) {
        return memberSocialAccountJpaRepository.findByProviderAndProviderId(provider, providerId)
            .map(MemberSocialAccountMapper::toDomain);
    }

    @Override
    public boolean existsByProviderAndProviderId(MemberSocialProvider provider, String providerId) {
        return memberSocialAccountJpaRepository.existsByProviderAndProviderId(provider, providerId);
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
