package com.tastyhouse.application.member.port.out.write;

import java.util.Optional;

public interface MemberSocialAccountStatePort {

    Optional<MemberSocialAccountState> findByProviderAndProviderId(String provider, String providerId);

    boolean existsByProviderAndProviderId(String provider, String providerId);

    MemberSocialAccountState save(MemberSocialAccountState state);
}
