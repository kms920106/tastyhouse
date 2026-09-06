package com.tastyhouse.application.auth.service.apple;

import com.tastyhouse.application.shared.marker.WebApp;
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
import com.tastyhouse.domain.member.repository.MemberRepository;
import com.tastyhouse.domain.member.repository.MemberSocialAccountRepository;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.security.token.AppleTempTokenRepository;
import com.tastyhouse.application.auth.port.out.SocialAuthorization;
import com.tastyhouse.application.auth.port.out.SocialCredential;
import com.tastyhouse.application.auth.port.out.SocialOAuthClient;
import com.tastyhouse.application.auth.port.out.SocialProfile;
import com.tastyhouse.application.auth.token.MemberJwtTokenProvider;
import com.tastyhouse.application.auth.token.MemberTokenService;
import com.tastyhouse.application.member.service.MemberCommandService;
import com.tastyhouse.application.auth.port.out.MemberJwtResult;
import com.tastyhouse.application.auth.port.out.SocialLinkResult;
import com.tastyhouse.application.auth.port.out.SocialLoginResult;
import com.tastyhouse.application.auth.port.out.SocialProfileResult;

@Service
@WebApp
public class AppleSocialLoginService {

    private final SocialOAuthClient appleOAuthClient;
    private final MemberCommandService memberCommandService;
    private final MemberRepository memberRepository;
    private final MemberSocialAccountRepository memberSocialAccountRepository;
    private final MemberTokenService tokenService;
    private final MemberJwtTokenProvider jwtTokenProvider;
    private final AppleTempTokenRepository appleTempTokenRepository;

    public AppleSocialLoginService(
        @Qualifier("appleOAuthClient") SocialOAuthClient appleOAuthClient,
        MemberCommandService memberCommandService,
        MemberRepository memberRepository,
        MemberSocialAccountRepository memberSocialAccountRepository,
        MemberTokenService tokenService,
        MemberJwtTokenProvider jwtTokenProvider,
        AppleTempTokenRepository appleTempTokenRepository
    ) {
        this.appleOAuthClient = appleOAuthClient;
        this.memberCommandService = memberCommandService;
        this.memberRepository = memberRepository;
        this.memberSocialAccountRepository = memberSocialAccountRepository;
        this.tokenService = tokenService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.appleTempTokenRepository = appleTempTokenRepository;
    }

    @Transactional
    public SocialLoginResult login(String authorizationCode) {

        SocialCredential credential = appleOAuthClient.exchange(SocialAuthorization.of(authorizationCode));
        SocialProfile appleUser = appleOAuthClient.fetchProfile(credential);

        String providerId = appleUser.providerId();

        Optional<MemberSocialAccount> socialAccountOpt =
            memberSocialAccountRepository.findByProviderAndProviderId(MemberSocialProvider.APPLE, providerId);

        if (socialAccountOpt.isPresent()) {
            MemberSocialAccount socialAccount = socialAccountOpt.get();

            socialAccount.updateProviderInfo(appleUser.email(), appleUser.nickname(), appleUser.profileImageUrl());
            memberCommandService.saveSocialAccount(socialAccount);

            Member member = memberRepository.findById(socialAccount.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
            return SocialLoginResult.ofLogin(issueJwt(member));
        }

        String appleEmail = appleUser.email();
        if (StringUtils.hasText(appleEmail) && memberRepository.existsByUsername(appleEmail)) {
            String appleTempToken = issueTempToken(credential.value());
            return SocialLoginResult.ofLinkingRequired(appleTempToken);
        }

        String appleTempToken = issueTempToken(credential.value());
        return SocialLoginResult.ofSignUpRequired(appleTempToken);
    }

    @Transactional
    public SocialLinkResult linkAccount(String appleTempToken, String smsVerifyToken) {
        if (jwtTokenProvider.isInvalidSmsVerifyToken(smsVerifyToken)) {
            throw new BusinessException(ErrorCode.MEMBER_PHONE_AUTH_EXPIRED);
        }

        String appleIdToken = appleTempTokenRepository.findAppleIdToken(appleTempToken);
        if (appleIdToken == null) {
            throw new BusinessException(ErrorCode.APPLE_TEMP_TOKEN_EXPIRED);
        }

        SocialProfile appleUser = appleOAuthClient.fetchProfile(SocialCredential.of(appleIdToken));
        String providerId = appleUser.providerId();

        if (memberSocialAccountRepository.existsByProviderAndProviderId(MemberSocialProvider.APPLE, providerId)) {
            throw new BusinessException(ErrorCode.SOCIAL_ACCOUNT_ALREADY_REGISTERED);
        }

        String phoneNumber = jwtTokenProvider.getPhoneNumberFromSmsVerifyToken(smsVerifyToken);
        Optional<Member> findMember = memberRepository.findByPhoneNumberAndStatusNot(phoneNumber, MemberStatus.DELETED);

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

        memberCommandService.saveSocialAccount(
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
            throw new BusinessException(ErrorCode.APPLE_TEMP_TOKEN_EXPIRED);
        }

        SocialProfile appleUser = appleOAuthClient.fetchProfile(SocialCredential.of(appleIdToken));
        String providerId = appleUser.providerId();

        if (memberSocialAccountRepository.existsByProviderAndProviderId(MemberSocialProvider.APPLE, providerId)) {
            throw new BusinessException(ErrorCode.SOCIAL_ACCOUNT_ALREADY_REGISTERED);
        }

        Member savedMember = memberCommandService.signUpSocial(
            username, nickname, fullName, gender, birthDate, phoneNumber,
            pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled, referrerNickname
        );

        memberCommandService.saveSocialAccount(
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
