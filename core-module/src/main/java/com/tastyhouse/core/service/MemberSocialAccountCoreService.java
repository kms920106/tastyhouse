package com.tastyhouse.core.service;

import com.tastyhouse.core.entity.user.MemberSocialAccount;
import com.tastyhouse.core.entity.user.SocialProvider;
import com.tastyhouse.core.repository.member.MemberSocialAccountJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberSocialAccountCoreService {

    private final MemberSocialAccountJpaRepository memberSocialAccountJpaRepository;

    @Transactional(readOnly = true)
    public Optional<MemberSocialAccount> findByProviderAndProviderId(SocialProvider provider, String providerId) {
        return memberSocialAccountJpaRepository.findByProviderAndProviderId(provider, providerId);
    }

    @Transactional(readOnly = true)
    public boolean existsByProviderAndProviderId(SocialProvider provider, String providerId) {
        return memberSocialAccountJpaRepository.existsByProviderAndProviderId(provider, providerId);
    }

    @Transactional
    @SuppressWarnings("null")
    public MemberSocialAccount save(MemberSocialAccount socialAccount) {
        return memberSocialAccountJpaRepository.save(socialAccount);
    }
}
