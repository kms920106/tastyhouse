package com.tastyhouse.application.auth.service.apple;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
import com.tastyhouse.application.auth.port.out.SocialOAuthClient;
import com.tastyhouse.application.auth.port.out.SocialProfile;
import com.tastyhouse.application.auth.port.out.SocialProfileResult;
import com.tastyhouse.application.auth.service.SocialOAuthFailures;
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
import com.tastyhouse.security.token.AppleTempTokenRepository;

@Service
public class AppleSocialLoginService {

    private final SocialOAuthClient appleOAuthClient;
    private final MemberRegistrationService memberRegistrationService;
    private final MemberLoadPort memberLoadPort;
    private final MemberSocialAccountLoadPort memberSocialAccountLoadPort;
    private final MemberSocialAccountSavePort memberSocialAccountSavePort;
    private final MemberTokenService tokenService;
    private final MemberJwtTokenProvider jwtTokenProvider;
    private final AppleTempTokenRepository appleTempTokenRepository;

    public AppleSocialLoginService(
        @Qualifier("appleOAuthClient") SocialOAuthClient appleOAuthClient,
        MemberRegistrationService memberRegistrationService,
        MemberLoadPort memberLoadPort,
        MemberSocialAccountLoadPort memberSocialAccountLoadPort,
        MemberSocialAccountSavePort memberSocialAccountSavePort,
        MemberTokenService tokenService,
        MemberJwtTokenProvider jwtTokenProvider,
        AppleTempTokenRepository appleTempTokenRepository
    ) {
        this.appleOAuthClient = appleOAuthClient;
        this.memberRegistrationService = memberRegistrationService;
        this.memberLoadPort = memberLoadPort;
        this.memberSocialAccountLoadPort = memberSocialAccountLoadPort;
        this.memberSocialAccountSavePort = memberSocialAccountSavePort;
        this.tokenService = tokenService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.appleTempTokenRepository = appleTempTokenRepository;
    }

    @Transactional
    public SocialLoginResult login(String authorizationCode) {

        SocialCredential credential = appleOAuthClient.exchange(SocialAuthorization.of(authorizationCode))
            .orElseThrow(SocialOAuthFailures::toException);
        SocialProfile appleUser = appleOAuthClient.fetchProfile(credential)
            .orElseThrow(SocialOAuthFailures::toException);

        String providerId = appleUser.providerId();

        Optional<MemberSocialAccount> socialAccountOpt =
            memberSocialAccountLoadPort.findByProviderAndProviderId(MemberSocialProvider.APPLE, providerId);

        if (socialAccountOpt.isPresent()) {
            MemberSocialAccount socialAccount = socialAccountOpt.get();

            socialAccount.updateProviderInfo(appleUser.email(), appleUser.nickname(), appleUser.profileImageUrl());
            memberSocialAccountSavePort.save(socialAccount);

            Member member = memberLoadPort.findById(socialAccount.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.MEMBER_NOT_FOUND));
            return SocialLoginResult.ofLogin(issueJwt(member));
        }

        String appleEmail = appleUser.email();
        if (StringUtils.hasText(appleEmail) && memberLoadPort.existsByUsername(appleEmail)) {
            String appleTempToken = issueTempToken(credential.value());
            return SocialLoginResult.ofLinkingRequired(appleTempToken);
        }

        String appleTempToken = issueTempToken(credential.value());
        return SocialLoginResult.ofSignUpRequired(appleTempToken);
    }

    @Transactional
    public SocialLinkResult linkAccount(String appleTempToken, String smsVerifyToken) {
        if (jwtTokenProvider.isInvalidSmsVerifyToken(smsVerifyToken)) {
            throw new ApplicationException(WebErrorCode.MEMBER_PHONE_AUTH_EXPIRED);
        }

        String appleIdToken = appleTempTokenRepository.findAppleIdToken(appleTempToken);
        if (appleIdToken == null) {
            throw new ApplicationException(WebErrorCode.APPLE_TEMP_TOKEN_EXPIRED);
        }

        SocialProfile appleUser = appleOAuthClient.fetchProfile(SocialCredential.of(appleIdToken))
            .orElseThrow(SocialOAuthFailures::toException);
        String providerId = appleUser.providerId();

        if (memberSocialAccountLoadPort.existsByProviderAndProviderId(MemberSocialProvider.APPLE, providerId)) {
            throw new ApplicationException(WebErrorCode.SOCIAL_ACCOUNT_ALREADY_REGISTERED);
        }

        String phoneNumber = jwtTokenProvider.getPhoneNumberFromSmsVerifyToken(smsVerifyToken);
        Optional<Member> findMember = memberLoadPort.findByPhoneNumberAndStatusNot(phoneNumber, MemberStatus.DELETED);

        if (findMember.isEmpty()) {
            return SocialLinkResult.ofSignUpRequired(
                appleTempToken,
                new SocialProfileResult(
                    providerId,
                    appleUser.email(),
                    appleUser.nickname(),
                    appleUser.profileImageUrl(),
                    appleUser.name(),
                    appleUser.phoneNumber(),
                    appleUser.gender(),
                    appleUser.birthYear(),
                    appleUser.birthMonth(),
                    appleUser.birthDay()
                )
            );
        }

        Member member = findMember.get();

        memberSocialAccountSavePort.save(
            MemberSocialAccount.of(
                member.getMemberId(),
                MemberSocialProvider.APPLE,
                providerId,
                appleUser.email(),
                appleUser.nickname(),
                appleUser.profileImageUrl()
            )
        );

        appleTempTokenRepository.delete(appleTempToken);

        return SocialLinkResult.ofLogin(issueJwt(member));
    }

    @Transactional
    public MemberJwtResult signUp(
        String appleTempToken,
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
        String appleIdToken = appleTempTokenRepository.findAppleIdToken(appleTempToken);
        if (appleIdToken == null) {
            throw new ApplicationException(WebErrorCode.APPLE_TEMP_TOKEN_EXPIRED);
        }

        SocialProfile appleUser = appleOAuthClient.fetchProfile(SocialCredential.of(appleIdToken))
            .orElseThrow(SocialOAuthFailures::toException);
        String providerId = appleUser.providerId();

        if (memberSocialAccountLoadPort.existsByProviderAndProviderId(MemberSocialProvider.APPLE, providerId)) {
            throw new ApplicationException(WebErrorCode.SOCIAL_ACCOUNT_ALREADY_REGISTERED);
        }

        Member savedMember = memberRegistrationService.signUpSocial(
            username, nickname, fullName, gender, birthDate, phoneNumber,
            pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled, referrerNickname
        );

        memberSocialAccountSavePort.save(
            MemberSocialAccount.of(
                savedMember.getMemberId(),
                MemberSocialProvider.APPLE,
                providerId,
                appleUser.email(),
                appleUser.nickname(),
                appleUser.profileImageUrl()
            )
        );

        appleTempTokenRepository.delete(appleTempToken);

        return issueJwt(savedMember);
    }

    private String issueTempToken(String appleIdToken) {
        String appleTempToken = UUID.randomUUID().toString();
        appleTempTokenRepository.save(appleTempToken, appleIdToken);
        return appleTempToken;
    }

    private MemberJwtResult issueJwt(Member member) {
        return tokenService.issue(member, false);
    }
}
