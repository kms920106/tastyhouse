package com.tastyhouse.infrastructure.member.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberSocialAccountJpaRepository extends JpaRepository<MemberSocialAccountJpaEntity, Long> {
    Optional<MemberSocialAccountJpaEntity> findByProviderAndProviderId(String provider, String providerId);

    boolean existsByProviderAndProviderId(String provider, String providerId);
}
