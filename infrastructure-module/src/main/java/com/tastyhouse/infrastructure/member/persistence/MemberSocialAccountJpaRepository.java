package com.tastyhouse.infrastructure.member.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.member.domain.model.MemberSocialProvider;

@Repository
public interface MemberSocialAccountJpaRepository extends JpaRepository<MemberSocialAccountJpaEntity, Long> {

    Optional<MemberSocialAccountJpaEntity> findByProviderAndProviderId(MemberSocialProvider provider, String providerId);

    boolean existsByProviderAndProviderId(MemberSocialProvider provider, String providerId);
}
