package com.tastyhouse.application.auth.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;
import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.domain.member.model.MemberGender;
import com.tastyhouse.domain.member.model.MemberSocialAccount;
import com.tastyhouse.domain.member.model.MemberSocialProvider;
import com.tastyhouse.domain.member.model.MemberStatus;
import com.tastyhouse.application.auth.port.out.MemberJwtResult;
import com.tastyhouse.application.auth.port.out.SocialAuthorization;
import com.tastyhouse.application.auth.port.out.SocialCredential;
import com.tastyhouse.application.auth.port.out.SocialLinkResult;
import com.tastyhouse.application.auth.port.out.SocialLoginResult;
import com.tastyhouse.application.auth.port.out.SocialOAuthClientPort;
import com.tastyhouse.application.auth.port.out.SocialProfile;
import com.tastyhouse.application.auth.port.out.SocialProfileResult;
import com.tastyhouse.application.auth.port.out.SocialProvider;
import com.tastyhouse.application.auth.token.MemberJwtTokenProvider;
import com.tastyhouse.application.auth.token.MemberTokenService;
import com.tastyhouse.application.member.port.out.write.MemberLoadPort;
import com.tastyhouse.application.member.port.out.write.MemberSocialAccountLoadPort;
import com.tastyhouse.application.member.port.out.write.MemberSocialAccountSavePort;
import com.tastyhouse.application.member.service.MemberRegistrationService;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shared.exception.WebErrorCode;
import com.tastyhouse.security.token.SocialTempTokenProvider;
import com.tastyhouse.security.token.SocialTempTokenRepository;

@Service
class SocialLoginService {

    private final SocialOAuthClientRouter socialOAuthClientRouter;
    private final MemberRegistrationService memberRegistrationService;
    private final MemberLoadPort memberLoadPort;
    private final MemberSocialAccountLoadPort memberSocialAccountLoadPort;
    private final MemberSocialAccountSavePort memberSocialAccountSavePort;
    private final MemberTokenService tokenService;
    private final MemberJwtTokenProvider jwtTokenProvider;
    private final SocialTempTokenRepository socialTempTokenRepository;

    public SocialLoginService(
        SocialOAuthClientRouter socialOAuthClientRouter,
        MemberRegistrationService memberRegistrationService,
        MemberLoadPort memberLoadPort,
        MemberSocialAccountLoadPort memberSocialAccountLoadPort,
        MemberSocialAccountSavePort memberSocialAccountSavePort,
        MemberTokenService tokenService,
        MemberJwtTokenProvider jwtTokenProvider,
        SocialTempTokenRepository socialTempTokenRepository
    ) {
        this.socialOAuthClientRouter = socialOAuthClientRouter;
        this.memberRegistrationService = memberRegistrationService;
        this.memberLoadPort = memberLoadPort;
        this.memberSocialAccountLoadPort = memberSocialAccountLoadPort;
        this.memberSocialAccountSavePort = memberSocialAccountSavePort;
        this.tokenService = tokenService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.socialTempTokenRepository = socialTempTokenRepository;
    }

    static SocialProvider providerOf(String provider) {
        return switch (MemberSocialProvider.from(provider)) {
            case KAKAO -> SocialProvider.KAKAO;
            case NAVER -> SocialProvider.NAVER;
            case FACEBOOK -> SocialProvider.FACEBOOK;
            case APPLE -> SocialProvider.APPLE;
            default -> throw new DomainException(DomainErrorCode.SOCIAL_PROVIDER_TYPE_UNKNOWN,
                DomainErrorCode.SOCIAL_PROVIDER_TYPE_UNKNOWN.getDefaultMessage() + ": " + provider);
        };
    }

    @Transactional
    public SocialLoginResult login(SocialProvider provider, SocialAuthorization authorization) {
        SocialOAuthClientPort client = socialOAuthClientRouter.resolve(provider);
        SocialCredential credential = client.exchange(authorization)
            .orElseThrow(SocialOAuthFailures::toException);
        SocialProfile profile = client.fetchProfile(credential)
            .orElseThrow(SocialOAuthFailures::toException);

        Optional<MemberSocialAccount> socialAccountOpt =
            memberSocialAccountLoadPort.findByProviderAndProviderId(memberProviderOf(provider), profile.providerId());

        if (socialAccountOpt.isPresent()) {
            MemberSocialAccount socialAccount = socialAccountOpt.get();
            socialAccount.updateProviderInfo(profile.email(), accountNickname(provider, profile), profile.profileImageUrl());
            memberSocialAccountSavePort.save(socialAccount);

            Member member = memberLoadPort.findById(socialAccount.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.MEMBER_NOT_FOUND));
            return SocialLoginResult.ofLogin(issueJwt(member));
        }

        String email = profile.email();
        if (StringUtils.hasText(email) && memberLoadPort.existsByUsername(email)) {
            return SocialLoginResult.ofLinkingRequired(issueTempToken(provider, credential.value()));
        }

        return SocialLoginResult.ofSignUpRequired(issueTempToken(provider, credential.value()));
    }

    @Transactional
    public SocialLinkResult linkAccount(SocialProvider provider, String tempToken, String smsVerifyToken) {
        if (jwtTokenProvider.isInvalidSmsVerifyToken(smsVerifyToken)) {
            throw new ApplicationException(WebErrorCode.MEMBER_PHONE_AUTH_EXPIRED);
        }

        SocialProfile profile = fetchProfileByTempToken(provider, tempToken);
        String providerId = profile.providerId();

        if (memberSocialAccountLoadPort.existsByProviderAndProviderId(memberProviderOf(provider), providerId)) {
            throw new ApplicationException(WebErrorCode.SOCIAL_ACCOUNT_ALREADY_REGISTERED);
        }

        String phoneNumber = jwtTokenProvider.getPhoneNumberFromSmsVerifyToken(smsVerifyToken);
        Optional<Member> memberOpt = memberLoadPort.findByPhoneNumberAndStatusNot(phoneNumber, MemberStatus.DELETED);

        if (memberOpt.isEmpty()) {
            return SocialLinkResult.ofSignUpRequired(
                tempToken,
                new SocialProfileResult(
                    providerId,
                    profile.email(),
                    profile.nickname(),
                    profile.profileImageUrl(),
                    profile.name(),
                    profile.phoneNumber(),
                    profile.gender(),
                    profile.birthYear(),
                    profile.birthMonth(),
                    profile.birthDay()
                )
            );
        }

        Member member = memberOpt.get();
        saveSocialAccount(provider, member, providerId, profile);

        socialTempTokenRepository.delete(tempTokenProviderOf(provider), tempToken);

        return SocialLinkResult.ofLogin(issueJwt(member));
    }

    @Transactional
    public MemberJwtResult signUp(
        SocialProvider provider,
        String tempToken,
        String username,
        String nickname,
        String fullName,
        MemberGender gender,
        Integer birthDate,
        String phoneNumber,
        boolean pushNotificationEnabled,
        boolean marketingInfoEnabled,
        boolean eventInfoEnabled,
        String referrerNickname
    ) {
        SocialProfile profile = fetchProfileByTempToken(provider, tempToken);
        String providerId = profile.providerId();

        if (memberSocialAccountLoadPort.existsByProviderAndProviderId(memberProviderOf(provider), providerId)) {
            throw new ApplicationException(WebErrorCode.SOCIAL_ACCOUNT_ALREADY_REGISTERED);
        }

        Member savedMember = memberRegistrationService.signUpSocial(
            username, nickname, fullName, gender, birthDate, phoneNumber,
            pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled, referrerNickname
        );

        saveSocialAccount(provider, savedMember, providerId, profile);

        socialTempTokenRepository.delete(tempTokenProviderOf(provider), tempToken);

        return issueJwt(savedMember);
    }

    private SocialProfile fetchProfileByTempToken(SocialProvider provider, String tempToken) {
        String credential = socialTempTokenRepository.findCredential(tempTokenProviderOf(provider), tempToken);
        if (credential == null) {
            throw SocialOAuthFailures.tempTokenExpired(provider);
        }
        return socialOAuthClientRouter.resolve(provider).fetchProfile(SocialCredential.of(credential))
            .orElseThrow(SocialOAuthFailures::toException);
    }

    private void saveSocialAccount(SocialProvider provider, Member member, String providerId, SocialProfile profile) {
        memberSocialAccountSavePort.save(
            MemberSocialAccount.of(
                member.getMemberId(),
                memberProviderOf(provider),
                providerId,
                profile.email(),
                accountNickname(provider, profile),
                profile.profileImageUrl()
            )
        );
    }

    private String issueTempToken(SocialProvider provider, String credential) {
        String tempToken = UUID.randomUUID().toString();
        socialTempTokenRepository.save(tempTokenProviderOf(provider), tempToken, credential);
        return tempToken;
    }

    private MemberJwtResult issueJwt(Member member) {
        return tokenService.issue(member, false);
    }

    private static String accountNickname(SocialProvider provider, SocialProfile profile) {
        return provider == SocialProvider.FACEBOOK ? profile.name() : profile.nickname();
    }

    private static MemberSocialProvider memberProviderOf(SocialProvider provider) {
        return switch (provider) {
            case KAKAO -> MemberSocialProvider.KAKAO;
            case NAVER -> MemberSocialProvider.NAVER;
            case FACEBOOK -> MemberSocialProvider.FACEBOOK;
            case APPLE -> MemberSocialProvider.APPLE;
        };
    }

    private static SocialTempTokenProvider tempTokenProviderOf(SocialProvider provider) {
        return SocialTempTokenProvider.valueOf(provider.name());
    }
}
