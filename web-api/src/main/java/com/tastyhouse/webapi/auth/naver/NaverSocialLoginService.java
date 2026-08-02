package com.tastyhouse.webapi.auth.naver;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.tastyhouse.domain.member.domain.model.Member;
import com.tastyhouse.domain.member.domain.model.MemberGender;
import com.tastyhouse.domain.member.domain.model.MemberSocialAccount;
import com.tastyhouse.domain.member.domain.model.MemberSocialProvider;
import com.tastyhouse.domain.member.domain.model.MemberStatus;
import com.tastyhouse.domain.member.domain.repository.MemberRepository;
import com.tastyhouse.domain.member.domain.repository.MemberSocialAccountRepository;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.external.oauth.spi.SocialAuthorization;
import com.tastyhouse.external.oauth.spi.SocialCredential;
import com.tastyhouse.external.oauth.spi.SocialOAuthClient;
import com.tastyhouse.external.oauth.spi.SocialProfile;
import com.tastyhouse.security.token.NaverTempTokenRedisRepository;
import com.tastyhouse.webapi.config.jwt.JwtTokenProvider;
import com.tastyhouse.webapi.config.jwt.service.TokenService;
import com.tastyhouse.webapi.member.service.MemberCommandService;
import com.tastyhouse.webapi.auth.response.AuthJwtResponse;
import com.tastyhouse.webapi.auth.response.AuthSocialLinkResponse;
import com.tastyhouse.webapi.auth.response.AuthSocialLoginResponse;
import com.tastyhouse.webapi.auth.response.AuthSocialProfileResponse;

@Service
public class NaverSocialLoginService {

    // SocialOAuthClient 구현이 제공자별로 4개이므로 빈 이름으로 명시 지정한다.
    private final SocialOAuthClient naverOAuthClient;
    private final MemberCommandService memberCommandService;
    private final MemberRepository memberRepository;
    private final MemberSocialAccountRepository memberSocialAccountRepository;
    private final TokenService tokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final NaverTempTokenRedisRepository naverTempTokenRedisRepository;

    public NaverSocialLoginService(
        @Qualifier("naverOAuthClient") SocialOAuthClient naverOAuthClient,
        MemberCommandService memberCommandService,
        MemberRepository memberRepository,
        MemberSocialAccountRepository memberSocialAccountRepository,
        TokenService tokenService,
        JwtTokenProvider jwtTokenProvider,
        NaverTempTokenRedisRepository naverTempTokenRedisRepository
    ) {
        this.naverOAuthClient = naverOAuthClient;
        this.memberCommandService = memberCommandService;
        this.memberRepository = memberRepository;
        this.memberSocialAccountRepository = memberSocialAccountRepository;
        this.tokenService = tokenService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.naverTempTokenRedisRepository = naverTempTokenRedisRepository;
    }

    // 인가 코드와 state로 네이버 로그인 처리
    // - 기존 회원: JWT 발급
    // - 신규 사용자: naverTempToken 반환 (NEEDS_SIGN_UP)
    // - 동일 이메일 일반가입 계정 존재: naverTempToken 반환 (NEEDS_LINKING)
    @Transactional
    public AuthSocialLoginResponse login(String authorizationCode, String state) {
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
            return AuthSocialLoginResponse.ofLogin(issueJwt(member));
        }

        // 소셜 계정은 없지만 동일 이메일로 일반가입한 회원이 존재하는 경우
        // → 사용자 동의 후 연동 처리가 필요하므로 NEEDS_LINKING 반환
        String naverEmail = naverUser.email();
        if (StringUtils.hasText(naverEmail) && memberRepository.existsByUsername(naverEmail)) {
            String naverTempToken = issueTempToken(credential.value());
            return AuthSocialLoginResponse.ofLinkingRequired(naverTempToken);
        }

        String naverTempToken = issueTempToken(credential.value());
        return AuthSocialLoginResponse.ofSignUpRequired(naverTempToken);
    }

    // 네이버 계정을 기존 일반가입 계정에 연동하고 JWT 발급
    // - smsVerifyToken으로 본인 확인 (전화번호로 Member 조회)
    // - naverTempToken으로 Redis에서 naverAccessToken 조회 후 사용자 정보 확인
    // - 전화번호로 가입된 회원이 없으면 NEEDS_SIGN_UP 반환 (naverTempToken 유지)
    // - MEMBER_SOCIAL_ACCOUNT INSERT 후 JWT 발급 (naverTempToken 삭제)
    @Transactional
    public AuthSocialLinkResponse linkAccount(String naverTempToken, String smsVerifyToken) {
        if (!jwtTokenProvider.validateSmsVerifyToken(smsVerifyToken)) {
            throw new BusinessException(ErrorCode.MEMBER_PHONE_AUTH_EXPIRED);
        }

        String naverAccessToken = naverTempTokenRedisRepository.findNaverAccessToken(naverTempToken);
        if (naverAccessToken == null) {
            throw new BusinessException(ErrorCode.NAVER_TEMP_TOKEN_EXPIRED);
        }

        SocialProfile naverUser = naverOAuthClient.fetchProfile(SocialCredential.of(naverAccessToken));
        String providerId = naverUser.providerId();

        // 이미 네이버 소셜 계정이 연동된 경우 중복 연동을 방지한다.
        if (memberSocialAccountRepository.existsByProviderAndProviderId(MemberSocialProvider.NAVER, providerId)) {
            throw new BusinessException(ErrorCode.SOCIAL_ACCOUNT_ALREADY_REGISTERED);
        }

        String phoneNumber = jwtTokenProvider.getPhoneNumberFromSmsVerifyToken(smsVerifyToken);
        Optional<Member> memberOpt = memberRepository.findByPhoneNumberAndStatusNot(phoneNumber, MemberStatus.DELETED);

        // 해당 전화번호로 가입된 회원이 없으면 회원가입이 필요한 상태로 응답한다.
        // naverTempToken은 /signup/naver에서 재사용해야 하므로 삭제하지 않는다.
        if (memberOpt.isEmpty()) {
            return AuthSocialLinkResponse.ofSignUpRequired(
                naverTempToken,
                new AuthSocialProfileResponse(
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

        naverTempTokenRedisRepository.delete(naverTempToken);

        return AuthSocialLinkResponse.ofLogin(issueJwt(member));
    }

    // 네이버 소셜 회원가입 처리 후 JWT 발급
    // - naverTempToken으로 Redis에서 naverAccessToken 조회
    // - 회원가입 완료 후 naverTempToken 삭제 (1회용)
    @Transactional
    public AuthJwtResponse signUp(String naverTempToken, String username, String nickname, String fullName,
                              MemberGender gender, Integer birthDate, String phoneNumber,
                              boolean pushNotificationEnabled, boolean marketingInfoEnabled,
                              boolean eventInfoEnabled, String referrerNickname) {
        String naverAccessToken = naverTempTokenRedisRepository.findNaverAccessToken(naverTempToken);
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

        naverTempTokenRedisRepository.delete(naverTempToken);

        return issueJwt(savedMember);
    }

    private String issueTempToken(String naverAccessToken) {
        String naverTempToken = UUID.randomUUID().toString();
        naverTempTokenRedisRepository.save(naverTempToken, naverAccessToken);
        return naverTempToken;
    }

    private AuthJwtResponse issueJwt(Member member) {
        return tokenService.issue(member, false);
    }
}
