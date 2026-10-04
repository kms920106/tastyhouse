package com.tastyhouse.application.auth.service.naver;

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
import com.tastyhouse.application.member.port.out.write.MemberPersistencePort;
import com.tastyhouse.application.member.port.out.write.MemberSocialAccountPersistencePort;
import com.tastyhouse.application.member.service.MemberRegistrationService;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shared.exception.WebErrorCode;
import com.tastyhouse.security.token.NaverTempTokenRepository;

@Service
public class NaverSocialLoginService {

    private final SocialOAuthClient naverOAuthClient;
    private final MemberRegistrationService memberRegistrationService;
    private final MemberPersistencePort memberPersistencePort;
    private final MemberSocialAccountPersistencePort memberSocialAccountPersistencePort;
    private final MemberTokenService tokenService;
    private final MemberJwtTokenProvider jwtTokenProvider;
    private final NaverTempTokenRepository naverTempTokenRepository;

    public NaverSocialLoginService(
        @Qualifier("naverOAuthClient") SocialOAuthClient naverOAuthClient,
        MemberRegistrationService memberRegistrationService,
        MemberPersistencePort memberPersistencePort,
        MemberSocialAccountPersistencePort memberSocialAccountPersistencePort,
        MemberTokenService tokenService,
        MemberJwtTokenProvider jwtTokenProvider,
        NaverTempTokenRepository naverTempTokenRepository
    ) {
        this.naverOAuthClient = naverOAuthClient;
        this.memberRegistrationService = memberRegistrationService;
        this.memberPersistencePort = memberPersistencePort;
        this.memberSocialAccountPersistencePort = memberSocialAccountPersistencePort;
        this.tokenService = tokenService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.naverTempTokenRepository = naverTempTokenRepository;
    }

    @Transactional
    public SocialLoginResult login(String authorizationCode, String state) {
        SocialCredential credential = naverOAuthClient.exchange(SocialAuthorization.of(authorizationCode, state))
            .orElseThrow(SocialOAuthFailures::toException);
        SocialProfile naverUser = naverOAuthClient.fetchProfile(credential)
            .orElseThrow(SocialOAuthFailures::toException);

        String providerId = naverUser.providerId();

        Optional<MemberSocialAccount> socialAccountOpt =
            memberSocialAccountPersistencePort.findByProviderAndProviderId(MemberSocialProvider.NAVER, providerId);

        if (socialAccountOpt.isPresent()) {
            MemberSocialAccount socialAccount = socialAccountOpt.get();
            socialAccount.updateProviderInfo(naverUser.email(), naverUser.nickname(), naverUser.profileImageUrl());
            memberSocialAccountPersistencePort.save(socialAccount);

            Member member = memberPersistencePort.findById(socialAccount.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.MEMBER_NOT_FOUND));
            return SocialLoginResult.ofLogin(issueJwt(member));
        }

        String naverEmail = naverUser.email();
        if (StringUtils.hasText(naverEmail) && memberPersistencePort.existsByUsername(naverEmail)) {
            String naverTempToken = issueTempToken(credential.value());
            return SocialLoginResult.ofLinkingRequired(naverTempToken);
        }

        String naverTempToken = issueTempToken(credential.value());
        return SocialLoginResult.ofSignUpRequired(naverTempToken);
    }

    @Transactional
    public SocialLinkResult linkAccount(String naverTempToken, String smsVerifyToken) {
        if (jwtTokenProvider.isInvalidSmsVerifyToken(smsVerifyToken)) {
            throw new ApplicationException(WebErrorCode.MEMBER_PHONE_AUTH_EXPIRED);
        }

        String naverAccessToken = naverTempTokenRepository.findNaverAccessToken(naverTempToken);
        if (naverAccessToken == null) {
            throw new ApplicationException(WebErrorCode.NAVER_TEMP_TOKEN_EXPIRED);
        }

        SocialProfile naverUser = naverOAuthClient.fetchProfile(SocialCredential.of(naverAccessToken))
            .orElseThrow(SocialOAuthFailures::toException);
        String providerId = naverUser.providerId();

        if (memberSocialAccountPersistencePort.existsByProviderAndProviderId(MemberSocialProvider.NAVER, providerId)) {
            throw new ApplicationException(WebErrorCode.SOCIAL_ACCOUNT_ALREADY_REGISTERED);
        }

        String phoneNumber = jwtTokenProvider.getPhoneNumberFromSmsVerifyToken(smsVerifyToken);
        Optional<Member> memberOpt = memberPersistencePort.findByPhoneNumberAndStatusNot(phoneNumber, MemberStatus.DELETED);

        if (memberOpt.isEmpty()) {
            return SocialLinkResult.ofSignUpRequired(
                naverTempToken,
                new SocialProfileResult(
                    providerId,
                    naverUser.email(),
                    naverUser.nickname(),
                    naverUser.profileImageUrl(),
                    naverUser.name(),
                    naverUser.phoneNumber(),
                    naverUser.gender(),
                    naverUser.birthYear(),
                    naverUser.birthMonth(),
                    naverUser.birthDay()
                )
            );
        }

        Member member = memberOpt.get();
        memberSocialAccountPersistencePort.save(
            MemberSocialAccount.of(
                member.getMemberId(), MemberSocialProvider.NAVER, providerId,
                naverUser.email(), naverUser.nickname(), naverUser.profileImageUrl()
            )
        );

        naverTempTokenRepository.delete(naverTempToken);

        return SocialLinkResult.ofLogin(issueJwt(member));
    }

    @Transactional
    public MemberJwtResult signUp(String naverTempToken, String username, String nickname, String fullName,
                              MemberGender gender, Integer birthDate, String phoneNumber,
                              boolean pushNotificationEnabled, boolean marketingInfoEnabled,
                              boolean eventInfoEnabled, String referrerNickname) {
        String naverAccessToken = naverTempTokenRepository.findNaverAccessToken(naverTempToken);
        if (naverAccessToken == null) {
            throw new ApplicationException(WebErrorCode.NAVER_TEMP_TOKEN_EXPIRED);
        }

        SocialProfile naverUser = naverOAuthClient.fetchProfile(SocialCredential.of(naverAccessToken))
            .orElseThrow(SocialOAuthFailures::toException);
        String providerId = naverUser.providerId();

        if (memberSocialAccountPersistencePort.existsByProviderAndProviderId(MemberSocialProvider.NAVER, providerId)) {
            throw new ApplicationException(WebErrorCode.SOCIAL_ACCOUNT_ALREADY_REGISTERED);
        }

        Member savedMember = memberRegistrationService.signUpSocial(
            username, nickname, fullName, gender, birthDate, phoneNumber,
            pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled, referrerNickname
        );

        memberSocialAccountPersistencePort.save(
            MemberSocialAccount.of(
                savedMember.getMemberId(), MemberSocialProvider.NAVER, providerId,
                naverUser.email(), naverUser.nickname(), naverUser.profileImageUrl()
            )
        );

        naverTempTokenRepository.delete(naverTempToken);

        return issueJwt(savedMember);
    }

    private String issueTempToken(String naverAccessToken) {
        String naverTempToken = UUID.randomUUID().toString();
        naverTempTokenRepository.save(naverTempToken, naverAccessToken);
        return naverTempToken;
    }

    private MemberJwtResult issueJwt(Member member) {
        return tokenService.issue(member, false);
    }
}
