package com.tastyhouse.core.repository.member;

import com.tastyhouse.core.entity.user.MemberSocialAccount;
import com.tastyhouse.core.entity.user.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberSocialAccountJpaRepository extends JpaRepository<MemberSocialAccount, Long> {

    Optional<MemberSocialAccount> findByProviderAndProviderId(SocialProvider provider, String providerId);

    boolean existsByProviderAndProviderId(SocialProvider provider, String providerId);
}
