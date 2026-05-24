package com.tastyhouse.webapi.auth.facebook;

import com.tastyhouse.core.domain.referral.application.ReferralCommandService;
import com.tastyhouse.core.domain.referral.application.dto.command.RegisterReferralCommand;
import com.tastyhouse.core.entity.user.Gender;
import com.tastyhouse.core.entity.user.Member;
import com.tastyhouse.core.entity.user.MemberSocialAccount;
import com.tastyhouse.core.entity.user.MemberStatus;
import com.tastyhouse.core.entity.user.SocialProvider;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.service.MemberCoreService;
import com.tastyhouse.core.service.MemberSocialAccountCoreService;
import com.tastyhouse.external.oauth.facebook.FacebookOAuthClient;
import com.tastyhouse.external.oauth.facebook.FacebookTokenDebugResponse;
import com.tastyhouse.external.oauth.facebook.FacebookUserInfoResponse;
import com.tastyhouse.webapi.auth.response.JwtResponse;
import com.tastyhouse.webapi.auth.response.SocialLinkResponse;
import com.tastyhouse.webapi.auth.response.SocialLoginResponse;
import com.tastyhouse.webapi.auth.response.SocialProfile;
import com.tastyhouse.webapi.config.jwt.JwtTokenProvider;
import com.tastyhouse.webapi.config.jwt.repository.FacebookTempTokenRedisRepository;
import com.tastyhouse.webapi.config.jwt.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FacebookSocialLoginService {

    @Value("${facebook.app-id}")
    private String facebookAppId;

    private final FacebookOAuthClient facebookOAuthClient;
    private final ReferralCommandService referralCommandService;
    private final MemberCoreService memberCoreService;
    private final MemberSocialAccountCoreService memberSocialAccountCoreService;
    private final TokenService tokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final FacebookTempTokenRedisRepository facebookTempTokenRedisRepository;

    // JS SDK 액세스 토큰으로 페이스북 로그인 처리
    // - 토큰 서버 검증 후 사용자 정보 조회
    // - 기존 회원: JWT 발급
    // - 신규 사용자: facebookTempToken 반환 (NEEDS_SIGN_UP)
    // - 동일 이메일 일반가입 계정 존재: facebookTempToken 반환 (NEEDS_LINKING)
    @Transactional
    public SocialLoginResponse login(String facebookAccessToken) {
        validateToken(facebookAccessToken);

        FacebookUserInfoResponse facebookUser = facebookOAuthClient.fetchUserInfo(facebookAccessToken);
        String providerId = facebookUser.id();

        Optional<MemberSocialAccount> socialAccountOpt =
            memberSocialAccountCoreService.findByProviderAndProviderId(SocialProvider.FACEBOOK, providerId);

        if (socialAccountOpt.isPresent()) {
            MemberSocialAccount socialAccount = socialAccountOpt.get();
            socialAccount.updateProviderInfo(facebookUser.email(), facebookUser.name(), facebookUser.getProfileImageUrl());

            Member member = memberCoreService.getById(socialAccount.getMemberId());
            return SocialLoginResponse.ofLogin(issueJwt(member));
        }

        // 소셜 계정은 없지만 동일 이메일로 일반가입한 회원이 존재하는 경우
        // → 사용자 동의 후 연동 처리가 필요하므로 NEEDS_LINKING 반환
        String facebookEmail = facebookUser.email();
        if (StringUtils.hasText(facebookEmail) && memberCoreService.existsByUsername(facebookEmail)) {
            String facebookTempToken = issueTempToken(facebookAccessToken);
            return SocialLoginResponse.ofLinkingRequired(facebookTempToken);
        }

        String facebookTempToken = issueTempToken(facebookAccessToken);
        return SocialLoginResponse.ofSignUpRequired(facebookTempToken);
    }

    // 페이스북 계정을 기존 일반가입 계정에 연동하고 JWT 발급
    // - phoneVerifyToken으로 본인 확인 (전화번호로 Member 조회)
    // - facebookTempToken으로 Redis에서 facebookAccessToken 조회 후 사용자 정보 확인
    // - 전화번호로 가입된 회원이 없으면 NEEDS_SIGN_UP 반환 (facebookTempToken 유지)
    // - MEMBER_SOCIAL_ACCOUNT INSERT 후 JWT 발급 (facebookTempToken 삭제)
    @Transactional
    public SocialLinkResponse linkAccount(String facebookTempToken, String phoneVerifyToken) {
        if (!jwtTokenProvider.validatePhoneVerifyToken(phoneVerifyToken)) {
            throw new BusinessException(ErrorCode.MEMBER_PHONE_AUTH_EXPIRED);
        }

        String facebookAccessToken = facebookTempTokenRedisRepository.findFacebookAccessToken(facebookTempToken);
        if (facebookAccessToken == null) {
            throw new BusinessException(ErrorCode.FACEBOOK_TEMP_TOKEN_EXPIRED);
        }

        FacebookUserInfoResponse facebookUser = facebookOAuthClient.fetchUserInfo(facebookAccessToken);
        String providerId = facebookUser.id();

        // 이미 페이스북 소셜 계정이 연동된 경우 중복 연동을 방지한다.
        if (memberSocialAccountCoreService.existsByProviderAndProviderId(SocialProvider.FACEBOOK, providerId)) {
            throw new BusinessException(ErrorCode.SOCIAL_ACCOUNT_ALREADY_REGISTERED);
        }

        String phoneNumber = jwtTokenProvider.getPhoneNumberFromPhoneVerifyToken(phoneVerifyToken);
        Optional<Member> memberOpt = memberCoreService.findByPhoneNumberAndStatusNot(phoneNumber, MemberStatus.DELETED);

        // 해당 전화번호로 가입된 회원이 없으면 회원가입이 필요한 상태로 응답한다.
        // facebookTempToken은 /signup/facebook에서 재사용해야 하므로 삭제하지 않는다.
        if (memberOpt.isEmpty()) {
            return SocialLinkResponse.ofSignUpRequired(
                facebookTempToken,
                new SocialProfile(
                    providerId,
                    facebookUser.email(),
                    null,
                    facebookUser.getProfileImageUrl(),
                    facebookUser.name(),
                    null,
                    null,
                    null,
                    null,
                    null
                )
            );
        }

        Member member = memberOpt.get();
        MemberSocialAccount socialAccount = MemberSocialAccount.of(
            member.getId(), SocialProvider.FACEBOOK, providerId,
            facebookUser.email(), facebookUser.name(), facebookUser.getProfileImageUrl()
        );
        memberSocialAccountCoreService.save(socialAccount);

        facebookTempTokenRedisRepository.delete(facebookTempToken);

        return SocialLinkResponse.ofLogin(issueJwt(member));
    }

    // 페이스북 소셜 회원가입 처리 후 JWT 발급
    // - facebookTempToken으로 Redis에서 facebookAccessToken 조회
    // - 회원가입 완료 후 facebookTempToken 삭제 (1회용)
    @Transactional
    public JwtResponse signUp(String facebookTempToken, String username, String nickname, String fullName,
                              Gender gender, Integer birthDate, String phoneNumber,
                              Boolean pushNotificationEnabled, Boolean marketingInfoEnabled,
                              Boolean eventInfoEnabled, String referrerNickname) {
        String facebookAccessToken = facebookTempTokenRedisRepository.findFacebookAccessToken(facebookTempToken);
        if (facebookAccessToken == null) {
            throw new BusinessException(ErrorCode.FACEBOOK_TEMP_TOKEN_EXPIRED);
        }

        FacebookUserInfoResponse facebookUser = facebookOAuthClient.fetchUserInfo(facebookAccessToken);
        String providerId = facebookUser.id();

        if (memberSocialAccountCoreService.existsByProviderAndProviderId(SocialProvider.FACEBOOK, providerId)) {
            throw new BusinessException(ErrorCode.SOCIAL_ACCOUNT_ALREADY_REGISTERED);
        }

        if (memberCoreService.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.MEMBER_USERNAME_DUPLICATED);
        }

        if (memberCoreService.existsByNickname(nickname)) {
            throw new BusinessException(ErrorCode.MEMBER_NICKNAME_DUPLICATED);
        }

        if (StringUtils.hasText(phoneNumber) &&
            memberCoreService.existsByPhoneNumberValueAndMemberStatusNot(phoneNumber, MemberStatus.DELETED)) {
            throw new BusinessException(ErrorCode.MEMBER_PHONE_ALREADY_REGISTERED);
        }

        Member member = Member.ofSocial(username, nickname, fullName, gender, birthDate, phoneNumber,
            pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled);
        memberCoreService.save(member);

        if (StringUtils.hasText(referrerNickname)) {
            if (referrerNickname.equals(nickname)) {
                throw new BusinessException(ErrorCode.REFERRAL_SELF_NOT_ALLOWED);
            }
            Member referrer = memberCoreService.findByNickname(referrerNickname)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFERRAL_REFERRER_NOT_FOUND));
            referralCommandService.register(
                new RegisterReferralCommand(referrer.getId(), member.getId())
            );
        }

        MemberSocialAccount socialAccount = MemberSocialAccount.of(
            member.getId(), SocialProvider.FACEBOOK, providerId,
            facebookUser.email(), facebookUser.name(), facebookUser.getProfileImageUrl()
        );
        memberSocialAccountCoreService.save(socialAccount);

        facebookTempTokenRedisRepository.delete(facebookTempToken);

        return issueJwt(member);
    }

    // Facebook 공식 문서 권장: 서버에서 액세스 토큰의 app_id가 자신의 앱과 일치하는지 반드시 검증
    private void validateToken(String facebookAccessToken) {
        FacebookTokenDebugResponse debugResponse = facebookOAuthClient.debugToken(facebookAccessToken);
        if (!debugResponse.isValid() || !facebookAppId.equals(debugResponse.getAppId())) {
            throw new BusinessException(ErrorCode.SOCIAL_OAUTH_FAILED);
        }
    }

    private String issueTempToken(String facebookAccessToken) {
        String facebookTempToken = UUID.randomUUID().toString();
        facebookTempTokenRedisRepository.save(facebookTempToken, facebookAccessToken);
        return facebookTempToken;
    }

    private JwtResponse issueJwt(Member member) {
        return tokenService.issue(member, false);
    }
}
