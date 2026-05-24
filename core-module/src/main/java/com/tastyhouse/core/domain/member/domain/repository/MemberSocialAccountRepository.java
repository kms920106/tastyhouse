package com.tastyhouse.core.domain.member.domain.repository;

import com.tastyhouse.core.domain.member.domain.model.MemberSocialAccount;
import com.tastyhouse.core.domain.member.domain.model.SocialProvider;

import java.util.Optional;

public interface MemberSocialAccountRepository {

    Optional<MemberSocialAccount> findByProviderAndProviderId(SocialProvider provider, String providerId);

    boolean existsByProviderAndProviderId(SocialProvider provider, String providerId);

    MemberSocialAccount save(MemberSocialAccount socialAccount);
}
