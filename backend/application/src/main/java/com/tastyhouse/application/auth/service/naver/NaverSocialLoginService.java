package com.tastyhouse.application.auth.service.naver;

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
import com.tastyhouse.security.token.NaverTempTokenRepository;
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
public class NaverSocialLoginService {

    private final SocialOAuthClient naverOAuthClient;
    private final MemberCommandService memberCommandService;
    private final MemberRepository memberRepository;
    private final MemberSocialAccountRepository memberSocialAccountRepository;
    private final MemberTokenService tokenService;
    private final MemberJwtTokenProvider jwtTokenProvider;
    private final NaverTempTokenRepository naverTempTokenRepository;

    public NaverSocialLoginService(
        @Qualifier("naverOAuthClient") SocialOAuthClient naverOAuthClient,
        MemberCommandService memberCommandService,
        MemberRepository memberRepository,
        MemberSocialAccountRepository memberSocialAccountRepository,
        MemberTokenService tokenService,
        MemberJwtTokenProvider jwtTokenProvider,
        NaverTempTokenRepository naverTempTokenRepository
    ) {
        this.naverOAuthClient = naverOAuthClient;
        this.memberCommandService = memberCommandService;
        this.memberRepository = memberRepository;
        this.memberSocialAccountRepository = memberSocialAccountRepository;
        this.tokenService = tokenService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.naverTempTokenRepository = naverTempTokenRepository;
    }

    @Transactional
    public SocialLoginResult login(String authorizationCode, String state) {
        SocialCredential credential = naverOAuthClient.exchange(SocialAuthorization.of(authorizationCode, state));
        SocialProfile naverUser = naverOAuthClient.fetchProfile(credential);

        String providerId = naverUser.providerId();

        Optional<MemberSocialAccount> socialAccountOpt =
            memberSocialAccountRepository.findByProviderAndProviderId(MemberSocialProvider.NAVER, providerId);

        if (socialAccountOpt.isPresent()) {
            MemberSocialAccount socialAccount = socialAccountOpt.get();
            socialAccount.updateProviderInfo(naverUser.email(), naverUser.nickname(), naverUser.profileImageUrl());
            memberCommandService.saveSocialAccount(socialAccount);

            Member member = memberRepository.findById(socialAccount.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
            return SocialLoginResult.ofLogin(issueJwt(member));
        }

        String naverEmail = naverUser.email();
        if (StringUtils.hasText(naverEmail) && memberRepository.existsByUsername(naverEmail)) {
            String naverTempToken = issueTempToken(credential.value());
            return SocialLoginResult.ofLinkingRequired(naverTempToken);
        }

        String naverTempToken = issueTempToken(credential.value());
        return SocialLoginResult.ofSignUpRequired(naverTempToken);
    }

    @Transactional
    public SocialLinkResult linkAccount(String naverTempToken, String smsVerifyToken) {
        if (jwtTokenProvider.isInvalidSmsVerifyToken(smsVerifyToken)) {
            throw new BusinessException(ErrorCode.MEMBER_PHONE_AUTH_EXPIRED);
        }

        String naverAccessToken = naverTempTokenRepository.findNaverAccessToken(naverTempToken);
        if (naverAccessToken == null) {
            throw new BusinessException(ErrorCode.NAVER_TEMP_TOKEN_EXPIRED);
        }

        SocialProfile naverUser = naverOAuthClient.fetchProfile(SocialCredential.of(naverAccessToken));
        String providerId = naverUser.providerId();

        if (memberSocialAccountRepository.existsByProviderAndProviderId(MemberSocialProvider.NAVER, providerId)) {
            throw new BusinessException(ErrorCode.SOCIAL_ACCOUNT_ALREADY_REGISTERED);
        }

        String phoneNumber = jwtTokenProvider.getPhoneNumberFromSmsVerifyToken(smsVerifyToken);
        Optional<Member> memberOpt = memberRepository.findByPhoneNumberAndStatusNot(phoneNumber, MemberStatus.DELETED);

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
        memberCommandService.saveSocialAccount(
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
            throw new BusinessException(ErrorCode.NAVER_TEMP_TOKEN_EXPIRED);
        }

        SocialProfile naverUser = naverOAuthClient.fetchProfile(SocialCredential.of(naverAccessToken));
        String providerId = naverUser.providerId();

        if (memberSocialAccountRepository.existsByProviderAndProviderId(MemberSocialProvider.NAVER, providerId)) {
            throw new BusinessException(ErrorCode.SOCIAL_ACCOUNT_ALREADY_REGISTERED);
        }

        Member savedMember = memberCommandService.signUpSocial(
            username, nickname, fullName, gender, birthDate, phoneNumber,
            pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled, referrerNickname
        );

        memberCommandService.saveSocialAccount(
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
