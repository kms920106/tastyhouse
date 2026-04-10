package com.tastyhouse.webapi.auth.kakao;

import com.tastyhouse.core.entity.referral.MemberReferral;
import com.tastyhouse.core.entity.user.Gender;
import com.tastyhouse.core.entity.user.Member;
import com.tastyhouse.core.entity.user.MemberSocialAccount;
import com.tastyhouse.core.entity.user.MemberStatus;
import com.tastyhouse.core.entity.user.SocialProvider;
import com.tastyhouse.core.exception.BusinessException;

import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.service.MemberCoreService;
import com.tastyhouse.core.service.MemberSocialAccountCoreService;
import com.tastyhouse.webapi.auth.response.JwtResponse;
import com.tastyhouse.webapi.auth.response.KakaoLoginResponse;
import com.tastyhouse.webapi.config.jwt.JwtTokenProvider;
import com.tastyhouse.webapi.config.jwt.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KakaoSocialLoginService {

    private final KakaoOAuthClient kakaoOAuthClient;
    private final MemberCoreService memberCoreService;
    private final MemberSocialAccountCoreService memberSocialAccountCoreService;
    private final TokenService tokenService;
    private final JwtTokenProvider jwtTokenProvider;

    // 인가 코드로 카카오 로그인 처리
    // - 기존 회원: JWT 발급
    // - 신규 사용자: needsSignUp=true + 카카오 프로필 반환
    @Transactional
    public KakaoLoginResponse login(String authorizationCode) {
        KakaoTokenResponse kakaoToken = kakaoOAuthClient.fetchToken(authorizationCode);
        KakaoUserInfoResponse kakaoUser = kakaoOAuthClient.fetchUserInfo(kakaoToken.accessToken());

        String providerId = String.valueOf(kakaoUser.id());

        Optional<MemberSocialAccount> socialAccountOpt =
            memberSocialAccountCoreService.findByProviderAndProviderId(SocialProvider.KAKAO, providerId);

        if (socialAccountOpt.isPresent()) {
            MemberSocialAccount socialAccount = socialAccountOpt.get();
            socialAccount.updateProviderInfo(kakaoUser.getEmail(), kakaoUser.getNickname(), kakaoUser.getProfileImageUrl());

            Member member = memberCoreService.getById(socialAccount.getMemberId());
            JwtResponse jwt = issueJwt(member.getUsername());
            return KakaoLoginResponse.ofLogin(kakaoToken.accessToken(), jwt);
        }

        // 소셜 계정은 없지만 동일 이메일로 일반가입한 회원이 존재하는 경우
        // → 사용자 동의 후 연동 처리가 필요하므로 NEEDS_LINKING 반환
        String kakaoEmail = kakaoUser.getEmail();
        if (StringUtils.hasText(kakaoEmail) && memberCoreService.existsByUsername(kakaoEmail)) {
            return KakaoLoginResponse.ofLinkingRequired(
                kakaoToken.accessToken(),
                providerId,
                kakaoUser.getEmail(),
                kakaoUser.getNickname(),
                kakaoUser.getProfileImageUrl(),
                kakaoUser.getName(),
                kakaoUser.getPhoneNumber()
            );
        }

        return KakaoLoginResponse.ofSignUpRequired(
            kakaoToken.accessToken(),
            providerId,
            kakaoUser.getEmail(),
            kakaoUser.getNickname(),
            kakaoUser.getProfileImageUrl(),
            kakaoUser.getName(),
            kakaoUser.getPhoneNumber()
        );
    }

    // 카카오 계정을 기존 일반가입 계정에 연동하고 JWT 발급
    // - phoneVerifyToken으로 본인 확인 (전화번호로 Member 조회)
    // - 카카오 액세스 토큰으로 사용자 정보 조회 후 providerId 검증
    // - MEMBER_SOCIAL_ACCOUNT INSERT 후 JWT 발급
    @Transactional
    public JwtResponse linkAccount(String kakaoAccessToken, String phoneVerifyToken) {
        if (!jwtTokenProvider.validatePhoneVerifyToken(phoneVerifyToken)) {
            throw new BusinessException(ErrorCode.MEMBER_PHONE_AUTH_EXPIRED);
        }

        KakaoUserInfoResponse kakaoUser = kakaoOAuthClient.fetchUserInfo(kakaoAccessToken);

        String providerId = String.valueOf(kakaoUser.id());

        if (memberSocialAccountCoreService.existsByProviderAndProviderId(SocialProvider.KAKAO, providerId)) {
            throw new BusinessException(ErrorCode.SOCIAL_ACCOUNT_ALREADY_REGISTERED);
        }

        String phoneNumber = jwtTokenProvider.getPhoneNumberFromPhoneVerifyToken(phoneVerifyToken);

        Member member = memberCoreService.findByPhoneNumberAndStatusNot(phoneNumber, MemberStatus.DELETED)
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        MemberSocialAccount socialAccount = new MemberSocialAccount(
            member.getId(), SocialProvider.KAKAO, providerId,
            kakaoUser.getEmail(), kakaoUser.getNickname(), kakaoUser.getProfileImageUrl()
        );
        memberSocialAccountCoreService.save(socialAccount);

        return issueJwt(member.getUsername());
    }

    // 카카오 소셜 회원가입 처리 후 JWT 발급
    @Transactional
    public JwtResponse signUp(String authorizationCode, String nickname, String fullName,
                              Gender gender, Integer birthDate, String phoneNumber,
                              String phoneVerifyToken,
                              Boolean pushNotificationEnabled, Boolean marketingInfoEnabled,
                              Boolean eventInfoEnabled, String referrerNickname) {
        KakaoTokenResponse kakaoToken = kakaoOAuthClient.fetchToken(authorizationCode);
        KakaoUserInfoResponse kakaoUser = kakaoOAuthClient.fetchUserInfo(kakaoToken.accessToken());

        String providerId = String.valueOf(kakaoUser.id());

        if (memberSocialAccountCoreService.existsByProviderAndProviderId(SocialProvider.KAKAO, providerId)) {
            throw new BusinessException(ErrorCode.SOCIAL_ACCOUNT_ALREADY_REGISTERED);
        }

        String username = kakaoUser.getEmail();
        if (!StringUtils.hasText(username)) {
            throw new BusinessException(ErrorCode.SOCIAL_EMAIL_REQUIRED);
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

        Member member = new Member(username, nickname, fullName, gender, birthDate, phoneNumber,
            pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled);
        memberCoreService.save(member);

        if (StringUtils.hasText(referrerNickname)) {
            if (referrerNickname.equals(nickname)) {
                throw new BusinessException(ErrorCode.REFERRAL_SELF_NOT_ALLOWED);
            }
            Member referrer = memberCoreService.findByNickname(referrerNickname)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFERRAL_REFERRER_NOT_FOUND));
            memberCoreService.saveReferral(
                MemberReferral.builder()
                    .referrerId(referrer.getId())
                    .refereeId(member.getId())
                    .build()
            );
        }

        MemberSocialAccount socialAccount = new MemberSocialAccount(
            member.getId(), SocialProvider.KAKAO, providerId,
            kakaoUser.getEmail(), kakaoUser.getNickname(), kakaoUser.getProfileImageUrl()
        );
        memberSocialAccountCoreService.save(socialAccount);

        return issueJwt(member.getUsername());
    }

    private JwtResponse issueJwt(String username) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            username, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        return tokenService.issue(authentication, false);
    }
}
