package com.tastyhouse.webapi.auth.facebook;

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
import com.tastyhouse.domain.exception.EntityNotFoundException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.external.oauth.spi.SocialAuthorization;
import com.tastyhouse.external.oauth.spi.SocialCredential;
import com.tastyhouse.external.oauth.spi.SocialOAuthClient;
import com.tastyhouse.external.oauth.spi.SocialProfile;
import com.tastyhouse.security.token.FacebookTempTokenRedisRepository;
import com.tastyhouse.webapi.config.jwt.JwtTokenProvider;
import com.tastyhouse.webapi.config.jwt.service.TokenService;
import com.tastyhouse.webapi.member.service.MemberCommandService;
import com.tastyhouse.webapi.auth.response.AuthJwtResponse;
import com.tastyhouse.webapi.auth.response.AuthSocialLinkResponse;
import com.tastyhouse.webapi.auth.response.AuthSocialLoginResponse;
import com.tastyhouse.webapi.auth.response.AuthSocialProfileResponse;

@Service
public class FacebookSocialLoginService {

    // SocialOAuthClient 구현이 제공자별로 4개이므로 빈 이름으로 명시 지정한다.
    private final SocialOAuthClient facebookOAuthClient;
    private final MemberCommandService memberCommandService;
    private final MemberRepository memberRepository;
    private final MemberSocialAccountRepository memberSocialAccountRepository;
    private final TokenService tokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final FacebookTempTokenRedisRepository facebookTempTokenRedisRepository;

    public FacebookSocialLoginService(
        @Qualifier("facebookOAuthClient") SocialOAuthClient facebookOAuthClient,
        MemberCommandService memberCommandService,
        MemberRepository memberRepository,
        MemberSocialAccountRepository memberSocialAccountRepository,
        TokenService tokenService,
        JwtTokenProvider jwtTokenProvider,
        FacebookTempTokenRedisRepository facebookTempTokenRedisRepository
    ) {
        this.facebookOAuthClient = facebookOAuthClient;
        this.memberCommandService = memberCommandService;
        this.memberRepository = memberRepository;
        this.memberSocialAccountRepository = memberSocialAccountRepository;
        this.tokenService = tokenService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.facebookTempTokenRedisRepository = facebookTempTokenRedisRepository;
    }

    // JS SDK 액세스 토큰으로 페이스북 로그인 처리
    // - 토큰 서버 검증 후 사용자 정보 조회
    // - 기존 회원: JWT 발급
    // - 신규 사용자: facebookTempToken 반환 (NEEDS_SIGN_UP)
    // - 동일 이메일 일반가입 계정 존재: facebookTempToken 반환 (NEEDS_LINKING)
    @Transactional
    public AuthSocialLoginResponse login(String facebookAccessToken) {
        // exchange가 Facebook 공식 문서 권장 서버측 app_id 검증을 수행한다(과거 이 클래스의 validateToken).
        SocialCredential credential = facebookOAuthClient.exchange(SocialAuthorization.of(facebookAccessToken));
        SocialProfile facebookUser = facebookOAuthClient.fetchProfile(credential);
        String providerId = facebookUser.providerId();

        Optional<MemberSocialAccount> socialAccountOpt =
            memberSocialAccountRepository.findByProviderAndProviderId(MemberSocialProvider.FACEBOOK, providerId);

        if (socialAccountOpt.isPresent()) {
            MemberSocialAccount socialAccount = socialAccountOpt.get();
            socialAccount.updateProviderInfo(facebookUser.email(), facebookUser.name(), facebookUser.profileImageUrl());
            memberCommandService.saveSocialAccount(socialAccount);

            Member member = memberRepository.findById(socialAccount.getMemberId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
            return AuthSocialLoginResponse.ofLogin(issueJwt(member));
        }

        // 소셜 계정은 없지만 동일 이메일로 일반가입한 회원이 존재하는 경우
        // → 사용자 동의 후 연동 처리가 필요하므로 NEEDS_LINKING 반환
        String facebookEmail = facebookUser.email();
        if (StringUtils.hasText(facebookEmail) && memberRepository.existsByUsername(facebookEmail)) {
            String facebookTempToken = issueTempToken(credential.value());
            return AuthSocialLoginResponse.ofLinkingRequired(facebookTempToken);
        }

        String facebookTempToken = issueTempToken(credential.value());
        return AuthSocialLoginResponse.ofSignUpRequired(facebookTempToken);
    }

    // 페이스북 계정을 기존 일반가입 계정에 연동하고 JWT 발급
    // - smsVerifyToken으로 본인 확인 (전화번호로 Member 조회)
    // - facebookTempToken으로 Redis에서 facebookAccessToken 조회 후 사용자 정보 확인
    // - 전화번호로 가입된 회원이 없으면 NEEDS_SIGN_UP 반환 (facebookTempToken 유지)
    // - MEMBER_SOCIAL_ACCOUNT INSERT 후 JWT 발급 (facebookTempToken 삭제)
    @Transactional
    public AuthSocialLinkResponse linkAccount(String facebookTempToken, String smsVerifyToken) {
        if (!jwtTokenProvider.validateSmsVerifyToken(smsVerifyToken)) {
            throw new BusinessException(ErrorCode.MEMBER_PHONE_AUTH_EXPIRED);
        }

        String facebookAccessToken = facebookTempTokenRedisRepository.findFacebookAccessToken(facebookTempToken);
        if (facebookAccessToken == null) {
            throw new BusinessException(ErrorCode.FACEBOOK_TEMP_TOKEN_EXPIRED);
        }

        SocialProfile facebookUser = facebookOAuthClient.fetchProfile(SocialCredential.of(facebookAccessToken));
        String providerId = facebookUser.providerId();

        // 이미 페이스북 소셜 계정이 연동된 경우 중복 연동을 방지한다.
        if (memberSocialAccountRepository.existsByProviderAndProviderId(MemberSocialProvider.FACEBOOK, providerId)) {
            throw new BusinessException(ErrorCode.SOCIAL_ACCOUNT_ALREADY_REGISTERED);
        }

        String phoneNumber = jwtTokenProvider.getPhoneNumberFromSmsVerifyToken(smsVerifyToken);
        Optional<Member> memberOpt = memberRepository.findByPhoneNumberAndStatusNot(phoneNumber, MemberStatus.DELETED);

        // 해당 전화번호로 가입된 회원이 없으면 회원가입이 필요한 상태로 응답한다.
        // facebookTempToken은 /signup/facebook에서 재사용해야 하므로 삭제하지 않는다.
        if (memberOpt.isEmpty()) {
            return AuthSocialLinkResponse.ofSignUpRequired(
                facebookTempToken,
                new AuthSocialProfileResponse(
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
        memberCommandService.saveSocialAccount(
            MemberSocialAccount.of(
                member.getMemberId(), MemberSocialProvider.FACEBOOK, providerId,
                facebookUser.email(), facebookUser.name(), facebookUser.profileImageUrl()
            )
        );

        facebookTempTokenRedisRepository.delete(facebookTempToken);

        return AuthSocialLinkResponse.ofLogin(issueJwt(member));
    }

    // 페이스북 소셜 회원가입 처리 후 JWT 발급
    // - facebookTempToken으로 Redis에서 facebookAccessToken 조회
    // - 회원가입 완료 후 facebookTempToken 삭제 (1회용)
    @Transactional
    public AuthJwtResponse signUp(String facebookTempToken, String username, String nickname, String fullName,
                              MemberGender gender, Integer birthDate, String phoneNumber,
                              boolean pushNotificationEnabled, boolean marketingInfoEnabled,
                              boolean eventInfoEnabled, String referrerNickname) {
        String facebookAccessToken = facebookTempTokenRedisRepository.findFacebookAccessToken(facebookTempToken);
        if (facebookAccessToken == null) {
            throw new BusinessException(ErrorCode.FACEBOOK_TEMP_TOKEN_EXPIRED);
        }

        SocialProfile facebookUser = facebookOAuthClient.fetchProfile(SocialCredential.of(facebookAccessToken));
        String providerId = facebookUser.providerId();

        if (memberSocialAccountRepository.existsByProviderAndProviderId(MemberSocialProvider.FACEBOOK, providerId)) {
            throw new BusinessException(ErrorCode.SOCIAL_ACCOUNT_ALREADY_REGISTERED);
        }

        Member savedMember = memberCommandService.signUpSocial(
            username, nickname, fullName, gender, birthDate, phoneNumber,
            pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled, referrerNickname
        );

        memberCommandService.saveSocialAccount(
            MemberSocialAccount.of(
                savedMember.getMemberId(), MemberSocialProvider.FACEBOOK, providerId,
                facebookUser.email(), facebookUser.name(), facebookUser.profileImageUrl()
            )
        );

        facebookTempTokenRedisRepository.delete(facebookTempToken);

        return issueJwt(savedMember);
    }

    private String issueTempToken(String facebookAccessToken) {
        String facebookTempToken = UUID.randomUUID().toString();
        facebookTempTokenRedisRepository.save(facebookTempToken, facebookAccessToken);
        return facebookTempToken;
    }

    private AuthJwtResponse issueJwt(Member member) {
        return tokenService.issue(member, false);
    }
}
