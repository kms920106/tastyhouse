package com.tastyhouse.application.auth.service.facebook;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
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
import com.tastyhouse.application.member.port.out.write.MemberPersistencePort;
import com.tastyhouse.application.member.port.out.write.MemberSocialAccountPersistencePort;
import com.tastyhouse.application.member.service.MemberRegistrationService;
import com.tastyhouse.application.shared.marker.WebApp;
import com.tastyhouse.security.token.FacebookTempTokenRepository;

@Service
@WebApp
public class FacebookSocialLoginService {

    private final SocialOAuthClient facebookOAuthClient;
    private final MemberRegistrationService memberRegistrationService;
    private final MemberPersistencePort memberPersistencePort;
    private final MemberSocialAccountPersistencePort memberSocialAccountPersistencePort;
    private final MemberTokenService tokenService;
    private final MemberJwtTokenProvider jwtTokenProvider;
    private final FacebookTempTokenRepository facebookTempTokenRepository;

    public FacebookSocialLoginService(
        @Qualifier("facebookOAuthClient") SocialOAuthClient facebookOAuthClient,
        MemberRegistrationService memberRegistrationService,
        MemberPersistencePort memberPersistencePort,
        MemberSocialAccountPersistencePort memberSocialAccountPersistencePort,
        MemberTokenService tokenService,
        MemberJwtTokenProvider jwtTokenProvider,
        FacebookTempTokenRepository facebookTempTokenRepository
    ) {
        this.facebookOAuthClient = facebookOAuthClient;
        this.memberRegistrationService = memberRegistrationService;
        this.memberPersistencePort = memberPersistencePort;
        this.memberSocialAccountPersistencePort = memberSocialAccountPersistencePort;
        this.tokenService = tokenService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.facebookTempTokenRepository = facebookTempTokenRepository;
    }

    @Transactional
    public SocialLoginResult login(String facebookAccessToken) {

        SocialCredential credential = facebookOAuthClient.exchange(SocialAuthorization.of(facebookAccessToken))
            .orElseThrow(SocialOAuthFailures::toException);
        SocialProfile facebookUser = facebookOAuthClient.fetchProfile(credential)
            .orElseThrow(SocialOAuthFailures::toException);
        String providerId = facebookUser.providerId();

        Optional<MemberSocialAccount> socialAccountOpt =
            memberSocialAccountPersistencePort.findByProviderAndProviderId(MemberSocialProvider.FACEBOOK, providerId);

        if (socialAccountOpt.isPresent()) {
            MemberSocialAccount socialAccount = socialAccountOpt.get();
            socialAccount.updateProviderInfo(facebookUser.email(), facebookUser.name(), facebookUser.profileImageUrl());
            memberSocialAccountPersistencePort.save(socialAccount);

            Member member = memberPersistencePort.findById(socialAccount.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
            return SocialLoginResult.ofLogin(issueJwt(member));
        }

        String facebookEmail = facebookUser.email();
        if (StringUtils.hasText(facebookEmail) && memberPersistencePort.existsByUsername(facebookEmail)) {
            String facebookTempToken = issueTempToken(credential.value());
            return SocialLoginResult.ofLinkingRequired(facebookTempToken);
        }

        String facebookTempToken = issueTempToken(credential.value());
        return SocialLoginResult.ofSignUpRequired(facebookTempToken);
    }

    @Transactional
    public SocialLinkResult linkAccount(String facebookTempToken, String smsVerifyToken) {
        if (jwtTokenProvider.isInvalidSmsVerifyToken(smsVerifyToken)) {
            throw new BusinessException(ErrorCode.MEMBER_PHONE_AUTH_EXPIRED);
        }

        String facebookAccessToken = facebookTempTokenRepository.findFacebookAccessToken(facebookTempToken);
        if (facebookAccessToken == null) {
            throw new BusinessException(ErrorCode.FACEBOOK_TEMP_TOKEN_EXPIRED);
        }

        SocialProfile facebookUser = facebookOAuthClient.fetchProfile(SocialCredential.of(facebookAccessToken))
            .orElseThrow(SocialOAuthFailures::toException);
        String providerId = facebookUser.providerId();

        if (memberSocialAccountPersistencePort.existsByProviderAndProviderId(MemberSocialProvider.FACEBOOK, providerId)) {
            throw new BusinessException(ErrorCode.SOCIAL_ACCOUNT_ALREADY_REGISTERED);
        }

        String phoneNumber = jwtTokenProvider.getPhoneNumberFromSmsVerifyToken(smsVerifyToken);
        Optional<Member> memberOpt = memberPersistencePort.findByPhoneNumberAndStatusNot(phoneNumber, MemberStatus.DELETED);

        if (memberOpt.isEmpty()) {
            return SocialLinkResult.ofSignUpRequired(
                facebookTempToken,
                new SocialProfileResult(
                    providerId,
                    facebookUser.email(),
                    facebookUser.nickname(),
                    facebookUser.profileImageUrl(),
                    facebookUser.name(),
                    facebookUser.phoneNumber(),
                    facebookUser.gender(),
                    facebookUser.birthYear(),
                    facebookUser.birthMonth(),
                    facebookUser.birthDay()
                )
            );
        }

        Member member = memberOpt.get();
        memberSocialAccountPersistencePort.save(
            MemberSocialAccount.of(
                member.getMemberId(), MemberSocialProvider.FACEBOOK, providerId,
                facebookUser.email(), facebookUser.name(), facebookUser.profileImageUrl()
            )
        );

        facebookTempTokenRepository.delete(facebookTempToken);

        return SocialLinkResult.ofLogin(issueJwt(member));
    }

    @Transactional
    public MemberJwtResult signUp(String facebookTempToken, String username, String nickname, String fullName,
                              MemberGender gender, Integer birthDate, String phoneNumber,
                              boolean pushNotificationEnabled, boolean marketingInfoEnabled,
                              boolean eventInfoEnabled, String referrerNickname) {
        String facebookAccessToken = facebookTempTokenRepository.findFacebookAccessToken(facebookTempToken);
        if (facebookAccessToken == null) {
            throw new BusinessException(ErrorCode.FACEBOOK_TEMP_TOKEN_EXPIRED);
        }

        SocialProfile facebookUser = facebookOAuthClient.fetchProfile(SocialCredential.of(facebookAccessToken))
            .orElseThrow(SocialOAuthFailures::toException);
        String providerId = facebookUser.providerId();

        if (memberSocialAccountPersistencePort.existsByProviderAndProviderId(MemberSocialProvider.FACEBOOK, providerId)) {
            throw new BusinessException(ErrorCode.SOCIAL_ACCOUNT_ALREADY_REGISTERED);
        }

        Member savedMember = memberRegistrationService.signUpSocial(
            username, nickname, fullName, gender, birthDate, phoneNumber,
            pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled, referrerNickname
        );

        memberSocialAccountPersistencePort.save(
            MemberSocialAccount.of(
                savedMember.getMemberId(), MemberSocialProvider.FACEBOOK, providerId,
                facebookUser.email(), facebookUser.name(), facebookUser.profileImageUrl()
            )
        );

        facebookTempTokenRepository.delete(facebookTempToken);

        return issueJwt(savedMember);
    }

    private String issueTempToken(String facebookAccessToken) {
        String facebookTempToken = UUID.randomUUID().toString();
        facebookTempTokenRepository.save(facebookTempToken, facebookAccessToken);
        return facebookTempToken;
    }

    private MemberJwtResult issueJwt(Member member) {
        return tokenService.issue(member, false);
    }
}
