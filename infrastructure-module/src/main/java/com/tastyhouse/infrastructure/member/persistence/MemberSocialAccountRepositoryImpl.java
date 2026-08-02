package com.tastyhouse.infrastructure.member.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.member.domain.model.MemberSocialAccount;
import com.tastyhouse.domain.member.domain.model.MemberSocialProvider;
import com.tastyhouse.domain.member.domain.repository.MemberSocialAccountRepository;

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

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        MemberSocialAccountJpaEntity entity = memberSocialAccountJpaRepository.findById(socialAccount.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 소셜 계정입니다: " + socialAccount.getId()));
        MemberSocialAccountMapper.applyChanges(entity, socialAccount);
        return MemberSocialAccountMapper.toDomain(entity);
    }
}
