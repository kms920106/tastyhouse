package com.tastyhouse.core.domain.member.domain.repository;

import java.util.Optional;

import com.tastyhouse.core.domain.member.domain.model.MemberSocialAccount;
import com.tastyhouse.core.domain.member.domain.model.MemberSocialProvider;

public interface MemberSocialAccountRepository {

    Optional<MemberSocialAccount> findByProviderAndProviderId(MemberSocialProvider provider, String providerId);

    boolean existsByProviderAndProviderId(MemberSocialProvider provider, String providerId);

    MemberSocialAccount save(MemberSocialAccount socialAccount);
}
