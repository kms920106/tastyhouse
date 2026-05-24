package com.tastyhouse.webapi.auth.kakao;

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
import com.tastyhouse.external.oauth.kakao.KakaoOAuthClient;
import com.tastyhouse.external.oauth.kakao.KakaoTokenResponse;
import com.tastyhouse.external.oauth.kakao.KakaoUserInfoResponse;
import com.tastyhouse.webapi.auth.response.JwtResponse;
import com.tastyhouse.webapi.auth.response.SocialLinkResponse;
import com.tastyhouse.webapi.auth.response.SocialLoginResponse;
import com.tastyhouse.webapi.auth.response.SocialProfile;
import com.tastyhouse.webapi.config.jwt.JwtTokenProvider;
import com.tastyhouse.webapi.config.jwt.repository.KakaoTempTokenRedisRepository;
import com.tastyhouse.webapi.config.jwt.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KakaoSocialLoginService {

    private final KakaoOAuthClient kakaoOAuthClient;
    private final ReferralCommandService referralCommandService;
    private final MemberCoreService memberCoreService;
    private final MemberSocialAccountCoreService memberSocialAccountCoreService;
    private final TokenService tokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final KakaoTempTokenRedisRepository kakaoTempTokenRedisRepository;

    // 인가 코드로 카카오 로그인 처리
    // - 기존 회원: JWT 발급
    // - 신규 사용자: kakaoTempToken 반환 (NEEDS_SIGN_UP)
    // - 동일 이메일 일반가입 계정 존재: kakaoTempToken 반환 (NEEDS_LINKING)
    @Transactional
    public SocialLoginResponse login(String authorizationCode) {
        KakaoTokenResponse kakaoToken = kakaoOAuthClient.fetchToken(authorizationCode);
        KakaoUserInfoResponse kakaoUser = kakaoOAuthClient.fetchUserInfo(kakaoToken.accessToken());

        String providerId = String.valueOf(kakaoUser.id());

        Optional<MemberSocialAccount> socialAccountOpt =
            memberSocialAccountCoreService.findByProviderAndProviderId(SocialProvider.KAKAO, providerId);

        if (socialAccountOpt.isPresent()) {
            MemberSocialAccount socialAccount = socialAccountOpt.get();
            socialAccount.updateProviderInfo(kakaoUser.getEmail(), kakaoUser.getNickname(), kakaoUser.getProfileImageUrl());

            Member member = memberCoreService.getById(socialAccount.getMemberId());
            return SocialLoginResponse.ofLogin(issueJwt(member));
        }

        // 소셜 계정은 없지만 동일 이메일로 일반가입한 회원이 존재하는 경우
        // → 사용자 동의 후 연동 처리가 필요하므로 NEEDS_LINKING 반환
        String kakaoEmail = kakaoUser.getEmail();
        if (StringUtils.hasText(kakaoEmail) && memberCoreService.existsByUsername(kakaoEmail)) {
            String kakaoTempToken = issueTempToken(kakaoToken.accessToken());
            return SocialLoginResponse.ofLinkingRequired(kakaoTempToken);
        }

        String kakaoTempToken = issueTempToken(kakaoToken.accessToken());
        return SocialLoginResponse.ofSignUpRequired(kakaoTempToken);
    }

    // 카카오 계정을 기존 일반가입 계정에 연동하고 JWT 발급
    // - phoneVerifyToken으로 본인 확인 (전화번호로 Member 조회)
    // - kakaoTempToken으로 Redis에서 kakaoAccessToken 조회 후 사용자 정보 확인
    // - 전화번호로 가입된 회원이 없으면 NEEDS_SIGN_UP 반환 (kakaoTempToken 유지)
    // - MEMBER_SOCIAL_ACCOUNT INSERT 후 JWT 발급 (kakaoTempToken 삭제)
    @Transactional
    public SocialLinkResponse linkAccount(String kakaoTempToken, String phoneVerifyToken) {
        if (!jwtTokenProvider.validatePhoneVerifyToken(phoneVerifyToken)) {
            throw new BusinessException(ErrorCode.MEMBER_PHONE_AUTH_EXPIRED);
        }

        String kakaoAccessToken = kakaoTempTokenRedisRepository.findKakaoAccessToken(kakaoTempToken);
        if (kakaoAccessToken == null) {
            throw new BusinessException(ErrorCode.KAKAO_TEMP_TOKEN_EXPIRED);
        }

        KakaoUserInfoResponse kakaoUser = kakaoOAuthClient.fetchUserInfo(kakaoAccessToken);
        String providerId = String.valueOf(kakaoUser.id());

        // 이미 카카오 소셜 계정이 연동된 경우 중복 연동을 방지한다.
        if (memberSocialAccountCoreService.existsByProviderAndProviderId(SocialProvider.KAKAO, providerId)) {
            throw new BusinessException(ErrorCode.SOCIAL_ACCOUNT_ALREADY_REGISTERED);
        }

        String phoneNumber = jwtTokenProvider.getPhoneNumberFromPhoneVerifyToken(phoneVerifyToken);
        Optional<Member> memberOpt = memberCoreService.findByPhoneNumberAndStatusNot(phoneNumber, MemberStatus.DELETED);

        // 해당 전화번호로 가입된 회원이 없으면 회원가입이 필요한 상태로 응답한다.
        // kakaoTempToken은 /signup/kakao에서 재사용해야 하므로 삭제하지 않는다.
        if (memberOpt.isEmpty()) {
            return SocialLinkResponse.ofSignUpRequired(
                kakaoTempToken,
                new SocialProfile(
                    providerId,
                    kakaoUser.getEmail(),
                    kakaoUser.getNickname(),
                    kakaoUser.getProfileImageUrl(),
                    kakaoUser.getName(),
                    kakaoUser.getPhoneNumber(),
                    kakaoUser.getGender(),
                    null,
                    null,
                    null
                )
            );
        }

        Member member = memberOpt.get();
        MemberSocialAccount socialAccount = MemberSocialAccount.of(
            member.getId(), SocialProvider.KAKAO, providerId,
            kakaoUser.getEmail(), kakaoUser.getNickname(), kakaoUser.getProfileImageUrl()
        );
        memberSocialAccountCoreService.save(socialAccount);

        kakaoTempTokenRedisRepository.delete(kakaoTempToken);

        return SocialLinkResponse.ofLogin(issueJwt(member));
    }

    // 카카오 소셜 회원가입 처리 후 JWT 발급
    // - kakaoTempToken으로 Redis에서 kakaoAccessToken 조회
    // - 회원가입 완료 후 kakaoTempToken 삭제 (1회용)
    @Transactional
    public JwtResponse signUp(
        String kakaoTempToken,
        String username,
        String nickname,
        String fullName,
        Gender gender,
        Integer birthDate,
        String phoneNumber,
        Boolean pushNotificationEnabled,
        Boolean marketingInfoEnabled,
        Boolean eventInfoEnabled,
        String referrerNickname
    ) {
        String kakaoAccessToken = kakaoTempTokenRedisRepository.findKakaoAccessToken(kakaoTempToken);
        if (kakaoAccessToken == null) {
            throw new BusinessException(ErrorCode.KAKAO_TEMP_TOKEN_EXPIRED);
        }

        KakaoUserInfoResponse kakaoUser = kakaoOAuthClient.fetchUserInfo(kakaoAccessToken);
        String providerId = String.valueOf(kakaoUser.id());

        boolean existsByProviderAndProviderId = memberSocialAccountCoreService.existsByProviderAndProviderId(SocialProvider.KAKAO, providerId);
        if (existsByProviderAndProviderId) {
            throw new BusinessException(ErrorCode.SOCIAL_ACCOUNT_ALREADY_REGISTERED);
        }

        boolean existsByUsername = memberCoreService.existsByUsername(username);
        if (existsByUsername) {
            throw new BusinessException(ErrorCode.MEMBER_USERNAME_DUPLICATED);
        }

        boolean existsByNickname = memberCoreService.existsByNickname(nickname);
        if (existsByNickname) {
            throw new BusinessException(ErrorCode.MEMBER_NICKNAME_DUPLICATED);
        }

        if (StringUtils.hasText(phoneNumber) &&
            memberCoreService.existsByPhoneNumberValueAndMemberStatusNot(phoneNumber, MemberStatus.DELETED)) {
            throw new BusinessException(ErrorCode.MEMBER_PHONE_ALREADY_REGISTERED);
        }

        Member savedMember = memberCoreService.save(
            Member.ofSocial(
                username,
                nickname,
                fullName,
                gender,
                birthDate,
                phoneNumber,
                pushNotificationEnabled,
                marketingInfoEnabled,
                eventInfoEnabled
            )
        );
        Long memberId = savedMember.getId();

        if (StringUtils.hasText(referrerNickname)) {
            if (referrerNickname.equals(nickname)) {
                throw new BusinessException(ErrorCode.REFERRAL_SELF_NOT_ALLOWED);
            }

            Member referrer = memberCoreService.findByNickname(referrerNickname)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFERRAL_REFERRER_NOT_FOUND));

            referralCommandService.register(
                new RegisterReferralCommand(referrer.getId(), memberId)
            );
        }

        memberSocialAccountCoreService.save(
            MemberSocialAccount.of(
                memberId,
                SocialProvider.KAKAO,
                providerId,
                kakaoUser.getEmail(),
                kakaoUser.getNickname(),
                kakaoUser.getProfileImageUrl()
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

    private JwtResponse issueJwt(Member member) {
        return tokenService.issue(member, false);
    }
}
