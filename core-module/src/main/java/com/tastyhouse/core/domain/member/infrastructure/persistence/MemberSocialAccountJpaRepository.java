package com.tastyhouse.core.domain.member.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.member.domain.model.MemberSocialAccount;
import com.tastyhouse.core.domain.member.domain.model.SocialProvider;

@Repository
public interface MemberSocialAccountJpaRepository extends JpaRepository<MemberSocialAccount, Long> {

    Optional<MemberSocialAccount> findByProviderAndProviderId(SocialProvider provider, String providerId);

    boolean existsByProviderAndProviderId(SocialProvider provider, String providerId);
}
