package com.tastyhouse.application.auth.service.kakao;

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
import com.tastyhouse.security.token.KakaoTempTokenRepository;

@Service
public class KakaoSocialLoginService {

    private final SocialOAuthClient kakaoOAuthClient;
    private final MemberRegistrationService memberRegistrationService;
    private final MemberPersistencePort memberPersistencePort;
    private final MemberSocialAccountPersistencePort memberSocialAccountPersistencePort;
    private final MemberTokenService tokenService;
    private final MemberJwtTokenProvider jwtTokenProvider;
    private final KakaoTempTokenRepository kakaoTempTokenRepository;

    public KakaoSocialLoginService(
        @Qualifier("kakaoOAuthClient") SocialOAuthClient kakaoOAuthClient,
        MemberRegistrationService memberRegistrationService,
        MemberPersistencePort memberPersistencePort,
        MemberSocialAccountPersistencePort memberSocialAccountPersistencePort,
        MemberTokenService tokenService,
        MemberJwtTokenProvider jwtTokenProvider,
        KakaoTempTokenRepository kakaoTempTokenRepository
    ) {
        this.kakaoOAuthClient = kakaoOAuthClient;
        this.memberRegistrationService = memberRegistrationService;
        this.memberPersistencePort = memberPersistencePort;
        this.memberSocialAccountPersistencePort = memberSocialAccountPersistencePort;
        this.tokenService = tokenService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.kakaoTempTokenRepository = kakaoTempTokenRepository;
    }

    @Transactional
    public SocialLoginResult login(String authorizationCode) {
        SocialCredential credential = kakaoOAuthClient.exchange(SocialAuthorization.of(authorizationCode))
            .orElseThrow(SocialOAuthFailures::toException);
        SocialProfile kakaoUser = kakaoOAuthClient.fetchProfile(credential)
            .orElseThrow(SocialOAuthFailures::toException);

        String providerId = kakaoUser.providerId();

        Optional<MemberSocialAccount> socialAccountOpt =
            memberSocialAccountPersistencePort.findByProviderAndProviderId(MemberSocialProvider.KAKAO, providerId);

        if (socialAccountOpt.isPresent()) {
            MemberSocialAccount socialAccount = socialAccountOpt.get();
            socialAccount.updateProviderInfo(kakaoUser.email(), kakaoUser.nickname(), kakaoUser.profileImageUrl());
            memberSocialAccountPersistencePort.save(socialAccount);

            Member member = memberPersistencePort.findById(socialAccount.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
            return SocialLoginResult.ofLogin(issueJwt(member));
        }

        String kakaoEmail = kakaoUser.email();
        if (StringUtils.hasText(kakaoEmail) && memberPersistencePort.existsByUsername(kakaoEmail)) {
            String kakaoTempToken = issueTempToken(credential.value());
            return SocialLoginResult.ofLinkingRequired(kakaoTempToken);
        }

        String kakaoTempToken = issueTempToken(credential.value());
        return SocialLoginResult.ofSignUpRequired(kakaoTempToken);
    }

    @Transactional
    public SocialLinkResult linkAccount(String kakaoTempToken, String smsVerifyToken) {
        if (jwtTokenProvider.isInvalidSmsVerifyToken(smsVerifyToken)) {
            throw new BusinessException(ErrorCode.MEMBER_PHONE_AUTH_EXPIRED);
        }

        String kakaoAccessToken = kakaoTempTokenRepository.findKakaoAccessToken(kakaoTempToken);
        if (kakaoAccessToken == null) {
            throw new BusinessException(ErrorCode.KAKAO_TEMP_TOKEN_EXPIRED);
        }

        SocialProfile kakaoUser = kakaoOAuthClient.fetchProfile(SocialCredential.of(kakaoAccessToken))
            .orElseThrow(SocialOAuthFailures::toException);
        String providerId = kakaoUser.providerId();

        if (memberSocialAccountPersistencePort.existsByProviderAndProviderId(MemberSocialProvider.KAKAO, providerId)) {
            throw new BusinessException(ErrorCode.SOCIAL_ACCOUNT_ALREADY_REGISTERED);
        }

        String phoneNumber = jwtTokenProvider.getPhoneNumberFromSmsVerifyToken(smsVerifyToken);
        Optional<Member> memberOpt = memberPersistencePort.findByPhoneNumberAndStatusNot(phoneNumber, MemberStatus.DELETED);

        if (memberOpt.isEmpty()) {
            return SocialLinkResult.ofSignUpRequired(
                kakaoTempToken,
                new SocialProfileResult(
                    providerId,
                    kakaoUser.email(),
                    kakaoUser.nickname(),
                    kakaoUser.profileImageUrl(),
                    kakaoUser.name(),
                    kakaoUser.phoneNumber(),
                    kakaoUser.gender(),
                    kakaoUser.birthYear(),
                    kakaoUser.birthMonth(),
                    kakaoUser.birthDay()
                )
            );
        }

        Member member = memberOpt.get();
        memberSocialAccountPersistencePort.save(
            MemberSocialAccount.of(
                member.getMemberId(), MemberSocialProvider.KAKAO, providerId,
                kakaoUser.email(), kakaoUser.nickname(), kakaoUser.profileImageUrl()
            )
        );

        kakaoTempTokenRepository.delete(kakaoTempToken);

        return SocialLinkResult.ofLogin(issueJwt(member));
    }

    @Transactional
    public MemberJwtResult signUp(
        String kakaoTempToken,
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
        String kakaoAccessToken = kakaoTempTokenRepository.findKakaoAccessToken(kakaoTempToken);
        if (kakaoAccessToken == null) {
            throw new BusinessException(ErrorCode.KAKAO_TEMP_TOKEN_EXPIRED);
        }

        SocialProfile kakaoUser = kakaoOAuthClient.fetchProfile(SocialCredential.of(kakaoAccessToken))
            .orElseThrow(SocialOAuthFailures::toException);
        String providerId = kakaoUser.providerId();

        if (memberSocialAccountPersistencePort.existsByProviderAndProviderId(MemberSocialProvider.KAKAO, providerId)) {
            throw new BusinessException(ErrorCode.SOCIAL_ACCOUNT_ALREADY_REGISTERED);
        }

        Member savedMember = memberRegistrationService.signUpSocial(
            username, nickname, fullName, gender, birthDate, phoneNumber,
            pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled, referrerNickname
        );

        memberSocialAccountPersistencePort.save(
            MemberSocialAccount.of(
                savedMember.getMemberId(),
                MemberSocialProvider.KAKAO,
                providerId,
                kakaoUser.email(),
                kakaoUser.nickname(),
                kakaoUser.profileImageUrl()
            )
        );

        kakaoTempTokenRepository.delete(kakaoTempToken);

        return issueJwt(savedMember);
    }

    private String issueTempToken(String kakaoAccessToken) {
        String kakaoTempToken = UUID.randomUUID().toString();
        kakaoTempTokenRepository.save(kakaoTempToken, kakaoAccessToken);
        return kakaoTempToken;
    }

    private MemberJwtResult issueJwt(Member member) {
        return tokenService.issue(member, false);
    }
}
