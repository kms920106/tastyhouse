package com.tastyhouse.core.domain.member.domain.repository;

import java.util.Optional;

import com.tastyhouse.core.domain.member.domain.model.MemberSocialAccount;
import com.tastyhouse.core.domain.member.domain.model.SocialProvider;

public interface MemberSocialAccountRepository {

    Optional<MemberSocialAccount> findByProviderAndProviderId(SocialProvider provider, String providerId);

    boolean existsByProviderAndProviderId(SocialProvider provider, String providerId);

    MemberSocialAccount save(MemberSocialAccount socialAccount);
}
