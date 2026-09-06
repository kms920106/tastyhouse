package com.tastyhouse.application.auth.service.kakao;

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
import com.tastyhouse.security.token.KakaoTempTokenRepository;
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
public class KakaoSocialLoginService {

    private final SocialOAuthClient kakaoOAuthClient;
    private final MemberCommandService memberCommandService;
    private final MemberRepository memberRepository;
    private final MemberSocialAccountRepository memberSocialAccountRepository;
    private final MemberTokenService tokenService;
    private final MemberJwtTokenProvider jwtTokenProvider;
    private final KakaoTempTokenRepository kakaoTempTokenRepository;

    public KakaoSocialLoginService(
        @Qualifier("kakaoOAuthClient") SocialOAuthClient kakaoOAuthClient,
        MemberCommandService memberCommandService,
        MemberRepository memberRepository,
        MemberSocialAccountRepository memberSocialAccountRepository,
        MemberTokenService tokenService,
        MemberJwtTokenProvider jwtTokenProvider,
        KakaoTempTokenRepository kakaoTempTokenRepository
    ) {
        this.kakaoOAuthClient = kakaoOAuthClient;
        this.memberCommandService = memberCommandService;
        this.memberRepository = memberRepository;
        this.memberSocialAccountRepository = memberSocialAccountRepository;
        this.tokenService = tokenService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.kakaoTempTokenRepository = kakaoTempTokenRepository;
    }

    @Transactional
    public SocialLoginResult login(String authorizationCode) {
        SocialCredential credential = kakaoOAuthClient.exchange(SocialAuthorization.of(authorizationCode));
        SocialProfile kakaoUser = kakaoOAuthClient.fetchProfile(credential);

        String providerId = kakaoUser.providerId();

        Optional<MemberSocialAccount> socialAccountOpt =
            memberSocialAccountRepository.findByProviderAndProviderId(MemberSocialProvider.KAKAO, providerId);

        if (socialAccountOpt.isPresent()) {
            MemberSocialAccount socialAccount = socialAccountOpt.get();
            socialAccount.updateProviderInfo(kakaoUser.email(), kakaoUser.nickname(), kakaoUser.profileImageUrl());
            memberCommandService.saveSocialAccount(socialAccount);

            Member member = memberRepository.findById(socialAccount.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
            return SocialLoginResult.ofLogin(issueJwt(member));
        }

        String kakaoEmail = kakaoUser.email();
        if (StringUtils.hasText(kakaoEmail) && memberRepository.existsByUsername(kakaoEmail)) {
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

        SocialProfile kakaoUser = kakaoOAuthClient.fetchProfile(SocialCredential.of(kakaoAccessToken));
        String providerId = kakaoUser.providerId();

        if (memberSocialAccountRepository.existsByProviderAndProviderId(MemberSocialProvider.KAKAO, providerId)) {
            throw new BusinessException(ErrorCode.SOCIAL_ACCOUNT_ALREADY_REGISTERED);
        }

        String phoneNumber = jwtTokenProvider.getPhoneNumberFromSmsVerifyToken(smsVerifyToken);
        Optional<Member> memberOpt = memberRepository.findByPhoneNumberAndStatusNot(phoneNumber, MemberStatus.DELETED);

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
        memberCommandService.saveSocialAccount(
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

        SocialProfile kakaoUser = kakaoOAuthClient.fetchProfile(SocialCredential.of(kakaoAccessToken));
        String providerId = kakaoUser.providerId();

        if (memberSocialAccountRepository.existsByProviderAndProviderId(MemberSocialProvider.KAKAO, providerId)) {
            throw new BusinessException(ErrorCode.SOCIAL_ACCOUNT_ALREADY_REGISTERED);
        }

        Member savedMember = memberCommandService.signUpSocial(
            username, nickname, fullName, gender, birthDate, phoneNumber,
            pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled, referrerNickname
        );

        memberCommandService.saveSocialAccount(
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
