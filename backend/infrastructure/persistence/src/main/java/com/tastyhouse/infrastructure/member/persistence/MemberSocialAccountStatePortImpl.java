package com.tastyhouse.infrastructure.member.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.member.port.out.write.MemberSocialAccountState;
import com.tastyhouse.application.member.port.out.write.MemberSocialAccountStatePort;

@Repository
public class MemberSocialAccountStatePortImpl implements MemberSocialAccountStatePort {
    private final MemberSocialAccountJpaRepository memberSocialAccountJpaRepository;

    public MemberSocialAccountStatePortImpl(MemberSocialAccountJpaRepository memberSocialAccountJpaRepository) {
        this.memberSocialAccountJpaRepository = memberSocialAccountJpaRepository;
    }

    @Override
    public Optional<MemberSocialAccountState> findByProviderAndProviderId(String provider, String providerId) {
        return memberSocialAccountJpaRepository.findByProviderAndProviderId(provider, providerId)
            .map(MemberSocialAccountMapper::toState);
    }

    @Override
    public boolean existsByProviderAndProviderId(String provider, String providerId) {
        return memberSocialAccountJpaRepository.existsByProviderAndProviderId(provider, providerId);
    }

    @Override
    public MemberSocialAccountState save(MemberSocialAccountState state) {
        if (state.id() == null) {
            MemberSocialAccountJpaEntity saved = memberSocialAccountJpaRepository.save(MemberSocialAccountMapper.toEntity(state));
            return MemberSocialAccountMapper.toState(saved);
        }

        MemberSocialAccountJpaEntity entity = memberSocialAccountJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 소셜 계정입니다: " + state.id()));
        MemberSocialAccountMapper.applyChanges(entity, state);
        return MemberSocialAccountMapper.toState(entity);
    }
}
