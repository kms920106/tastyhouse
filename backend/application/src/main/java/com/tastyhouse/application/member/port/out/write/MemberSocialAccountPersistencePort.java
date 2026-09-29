package com.tastyhouse.application.member.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.member.model.MemberSocialAccount;
import com.tastyhouse.domain.member.model.MemberSocialProvider;

public interface MemberSocialAccountPersistencePort {

    Optional<MemberSocialAccount> findByProviderAndProviderId(MemberSocialProvider provider, String providerId);

    boolean existsByProviderAndProviderId(MemberSocialProvider provider, String providerId);

    MemberSocialAccount save(MemberSocialAccount socialAccount);
}
