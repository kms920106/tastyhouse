package com.tastyhouse.webapi.auth.kakao;

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
import com.tastyhouse.external.oauth.spi.SocialAuthorization;
import com.tastyhouse.external.oauth.spi.SocialCredential;
import com.tastyhouse.external.oauth.spi.SocialOAuthClient;
import com.tastyhouse.external.oauth.spi.SocialProfile;
import com.tastyhouse.security.token.KakaoTempTokenRedisRepository;
import com.tastyhouse.webapi.config.jwt.JwtTokenProvider;
import com.tastyhouse.webapi.config.jwt.service.TokenService;
import com.tastyhouse.webapi.member.service.MemberCommandService;
import com.tastyhouse.webapi.auth.response.AuthJwtResponse;
import com.tastyhouse.webapi.auth.response.AuthSocialLinkResponse;
import com.tastyhouse.webapi.auth.response.AuthSocialLoginResponse;
import com.tastyhouse.webapi.auth.response.AuthSocialProfileResponse;

@Service
public class KakaoSocialLoginService {

    // SocialOAuthClient 구현이 제공자별로 4개이므로 빈 이름으로 명시 지정한다.
    private final SocialOAuthClient kakaoOAuthClient;
    private final MemberCommandService memberCommandService;
    private final MemberRepository memberRepository;
    private final MemberSocialAccountRepository memberSocialAccountRepository;
    private final TokenService tokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final KakaoTempTokenRedisRepository kakaoTempTokenRedisRepository;

    public KakaoSocialLoginService(
        @Qualifier("kakaoOAuthClient") SocialOAuthClient kakaoOAuthClient,
        MemberCommandService memberCommandService,
        MemberRepository memberRepository,
        MemberSocialAccountRepository memberSocialAccountRepository,
        TokenService tokenService,
        JwtTokenProvider jwtTokenProvider,
        KakaoTempTokenRedisRepository kakaoTempTokenRedisRepository
    ) {
        this.kakaoOAuthClient = kakaoOAuthClient;
        this.memberCommandService = memberCommandService;
        this.memberRepository = memberRepository;
        this.memberSocialAccountRepository = memberSocialAccountRepository;
        this.tokenService = tokenService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.kakaoTempTokenRedisRepository = kakaoTempTokenRedisRepository;
    }

    // 인가 코드로 카카오 로그인 처리
    // - 기존 회원: JWT 발급
    // - 신규 사용자: kakaoTempToken 반환 (NEEDS_SIGN_UP)
    // - 동일 이메일 일반가입 계정 존재: kakaoTempToken 반환 (NEEDS_LINKING)
    @Transactional
    public AuthSocialLoginResponse login(String authorizationCode) {
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
            return AuthSocialLoginResponse.ofLogin(issueJwt(member));
        }

        // 소셜 계정은 없지만 동일 이메일로 일반가입한 회원이 존재하는 경우
        // → 사용자 동의 후 연동 처리가 필요하므로 NEEDS_LINKING 반환
        String kakaoEmail = kakaoUser.email();
        if (StringUtils.hasText(kakaoEmail) && memberRepository.existsByUsername(kakaoEmail)) {
            String kakaoTempToken = issueTempToken(credential.value());
            return AuthSocialLoginResponse.ofLinkingRequired(kakaoTempToken);
        }

        String kakaoTempToken = issueTempToken(credential.value());
        return AuthSocialLoginResponse.ofSignUpRequired(kakaoTempToken);
    }

    // 카카오 계정을 기존 일반가입 계정에 연동하고 JWT 발급
    // - smsVerifyToken으로 본인 확인 (전화번호로 Member 조회)
    // - kakaoTempToken으로 Redis에서 kakaoAccessToken 조회 후 사용자 정보 확인
    // - 전화번호로 가입된 회원이 없으면 NEEDS_SIGN_UP 반환 (kakaoTempToken 유지)
    // - MEMBER_SOCIAL_ACCOUNT INSERT 후 JWT 발급 (kakaoTempToken 삭제)
    @Transactional
    public AuthSocialLinkResponse linkAccount(String kakaoTempToken, String smsVerifyToken) {
        if (!jwtTokenProvider.validateSmsVerifyToken(smsVerifyToken)) {
            throw new BusinessException(ErrorCode.MEMBER_PHONE_AUTH_EXPIRED);
        }

        String kakaoAccessToken = kakaoTempTokenRedisRepository.findKakaoAccessToken(kakaoTempToken);
        if (kakaoAccessToken == null) {
            throw new BusinessException(ErrorCode.KAKAO_TEMP_TOKEN_EXPIRED);
        }

        SocialProfile kakaoUser = kakaoOAuthClient.fetchProfile(SocialCredential.of(kakaoAccessToken));
        String providerId = kakaoUser.providerId();

        // 이미 카카오 소셜 계정이 연동된 경우 중복 연동을 방지한다.
        if (memberSocialAccountRepository.existsByProviderAndProviderId(MemberSocialProvider.KAKAO, providerId)) {
            throw new BusinessException(ErrorCode.SOCIAL_ACCOUNT_ALREADY_REGISTERED);
        }

        String phoneNumber = jwtTokenProvider.getPhoneNumberFromSmsVerifyToken(smsVerifyToken);
        Optional<Member> memberOpt = memberRepository.findByPhoneNumberAndStatusNot(phoneNumber, MemberStatus.DELETED);

        // 해당 전화번호로 가입된 회원이 없으면 회원가입이 필요한 상태로 응답한다.
        // kakaoTempToken은 /signup/kakao에서 재사용해야 하므로 삭제하지 않는다.
        if (memberOpt.isEmpty()) {
            return AuthSocialLinkResponse.ofSignUpRequired(
                kakaoTempToken,
                new AuthSocialProfileResponse(
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

        kakaoTempTokenRedisRepository.delete(kakaoTempToken);

        return AuthSocialLinkResponse.ofLogin(issueJwt(member));
    }

    // 카카오 소셜 회원가입 처리 후 JWT 발급
    // - kakaoTempToken으로 Redis에서 kakaoAccessToken 조회
    // - 회원가입 완료 후 kakaoTempToken 삭제 (1회용)
    @Transactional
    public AuthJwtResponse signUp(
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
        String kakaoAccessToken = kakaoTempTokenRedisRepository.findKakaoAccessToken(kakaoTempToken);
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

        kakaoTempTokenRedisRepository.delete(kakaoTempToken);

        return issueJwt(savedMember);
    }

    private String issueTempToken(String kakaoAccessToken) {
        String kakaoTempToken = UUID.randomUUID().toString();
        kakaoTempTokenRedisRepository.save(kakaoTempToken, kakaoAccessToken);
        return kakaoTempToken;
    }

    private AuthJwtResponse issueJwt(Member member) {
        return tokenService.issue(member, false);
    }
}
