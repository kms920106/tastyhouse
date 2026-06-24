package com.tastyhouse.webapi.auth.naver;

import com.tastyhouse.core.domain.member.application.MemberCommandService;
import com.tastyhouse.core.domain.member.application.MemberQueryService;
import com.tastyhouse.core.domain.member.domain.model.Gender;
import com.tastyhouse.core.domain.member.domain.model.Member;
import com.tastyhouse.core.domain.member.domain.model.MemberSocialAccount;
import com.tastyhouse.core.domain.member.domain.model.MemberStatus;
import com.tastyhouse.core.domain.member.domain.model.SocialProvider;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.external.oauth.naver.NaverOAuthClient;
import com.tastyhouse.external.oauth.naver.NaverTokenResponse;
import com.tastyhouse.external.oauth.naver.NaverUserInfoResponse;
import com.tastyhouse.webapi.auth.response.JwtResponse;
import com.tastyhouse.webapi.auth.response.SocialLinkResponse;
import com.tastyhouse.webapi.auth.response.SocialLoginResponse;
import com.tastyhouse.webapi.auth.response.SocialProfile;
import com.tastyhouse.webapi.config.jwt.JwtTokenProvider;
import com.tastyhouse.webapi.config.jwt.repository.NaverTempTokenRedisRepository;
import com.tastyhouse.webapi.config.jwt.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NaverSocialLoginService {

    private final NaverOAuthClient naverOAuthClient;
    private final MemberCommandService memberCommandService;
    private final MemberQueryService memberQueryService;
    private final TokenService tokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final NaverTempTokenRedisRepository naverTempTokenRedisRepository;

    // 인가 코드와 state로 네이버 로그인 처리
    // - 기존 회원: JWT 발급
    // - 신규 사용자: naverTempToken 반환 (NEEDS_SIGN_UP)
    // - 동일 이메일 일반가입 계정 존재: naverTempToken 반환 (NEEDS_LINKING)
    @Transactional
    public SocialLoginResponse login(String authorizationCode, String state) {
        NaverTokenResponse naverToken = naverOAuthClient.fetchToken(authorizationCode, state);
        NaverUserInfoResponse naverUser = naverOAuthClient.fetchUserInfo(naverToken.accessToken());

        String providerId = naverUser.getProviderId();

        Optional<MemberSocialAccount> socialAccountOpt =
            memberQueryService.findSocialAccount(SocialProvider.NAVER, providerId);

        if (socialAccountOpt.isPresent()) {
            MemberSocialAccount socialAccount = socialAccountOpt.get();
            socialAccount.updateProviderInfo(naverUser.getEmail(), naverUser.getNickname(), naverUser.getProfileImageUrl());

            Member member = memberQueryService.getById(new MemberId(socialAccount.getMemberId()));
            return SocialLoginResponse.ofLogin(issueJwt(member));
        }

        // 소셜 계정은 없지만 동일 이메일로 일반가입한 회원이 존재하는 경우
        // → 사용자 동의 후 연동 처리가 필요하므로 NEEDS_LINKING 반환
        String naverEmail = naverUser.getEmail();
        if (StringUtils.hasText(naverEmail) && memberQueryService.existsByUsername(naverEmail)) {
            String naverTempToken = issueTempToken(naverToken.accessToken());
            return SocialLoginResponse.ofLinkingRequired(naverTempToken);
        }

        String naverTempToken = issueTempToken(naverToken.accessToken());
        return SocialLoginResponse.ofSignUpRequired(naverTempToken);
    }

    // 네이버 계정을 기존 일반가입 계정에 연동하고 JWT 발급
    // - phoneVerifyToken으로 본인 확인 (전화번호로 Member 조회)
    // - naverTempToken으로 Redis에서 naverAccessToken 조회 후 사용자 정보 확인
    // - 전화번호로 가입된 회원이 없으면 NEEDS_SIGN_UP 반환 (naverTempToken 유지)
    // - MEMBER_SOCIAL_ACCOUNT INSERT 후 JWT 발급 (naverTempToken 삭제)
    @Transactional
    public SocialLinkResponse linkAccount(String naverTempToken, String phoneVerifyToken) {
        if (!jwtTokenProvider.validatePhoneVerifyToken(phoneVerifyToken)) {
            throw new BusinessException(ErrorCode.MEMBER_PHONE_AUTH_EXPIRED);
        }

        String naverAccessToken = naverTempTokenRedisRepository.findNaverAccessToken(naverTempToken);
        if (naverAccessToken == null) {
            throw new BusinessException(ErrorCode.NAVER_TEMP_TOKEN_EXPIRED);
        }

        NaverUserInfoResponse naverUser = naverOAuthClient.fetchUserInfo(naverAccessToken);
        String providerId = naverUser.getProviderId();

        // 이미 네이버 소셜 계정이 연동된 경우 중복 연동을 방지한다.
        if (memberQueryService.existsSocialAccount(SocialProvider.NAVER, providerId)) {
            throw new BusinessException(ErrorCode.SOCIAL_ACCOUNT_ALREADY_REGISTERED);
        }

        String phoneNumber = jwtTokenProvider.getPhoneNumberFromPhoneVerifyToken(phoneVerifyToken);
        Optional<Member> memberOpt = memberQueryService.findByPhoneNumberAndStatusNot(phoneNumber, MemberStatus.DELETED);

        // 해당 전화번호로 가입된 회원이 없으면 회원가입이 필요한 상태로 응답한다.
        // naverTempToken은 /signup/naver에서 재사용해야 하므로 삭제하지 않는다.
        if (memberOpt.isEmpty()) {
            return SocialLinkResponse.ofSignUpRequired(
                naverTempToken,
                new SocialProfile(
                    providerId,
                    naverUser.getEmail(),
                    naverUser.getNickname(),
                    naverUser.getProfileImageUrl(),
                    naverUser.getName(),
                    naverUser.getMobile(),
                    naverUser.getGender(),
                    naverUser.getBirthYear(),
                    naverUser.getBirthMonth(),
                    naverUser.getBirthDay()
                )
            );
        }

        Member member = memberOpt.get();
        memberCommandService.saveSocialAccount(
            MemberSocialAccount.of(
                member.getId(), SocialProvider.NAVER, providerId,
                naverUser.getEmail(), naverUser.getNickname(), naverUser.getProfileImageUrl()
            )
        );

        naverTempTokenRedisRepository.delete(naverTempToken);

        return SocialLinkResponse.ofLogin(issueJwt(member));
    }

    // 네이버 소셜 회원가입 처리 후 JWT 발급
    // - naverTempToken으로 Redis에서 naverAccessToken 조회
    // - 회원가입 완료 후 naverTempToken 삭제 (1회용)
    @Transactional
    public JwtResponse signUp(String naverTempToken, String username, String nickname, String fullName,
                              Gender gender, Integer birthDate, String phoneNumber,
                              boolean pushNotificationEnabled, boolean marketingInfoEnabled,
                              boolean eventInfoEnabled, String referrerNickname) {
        String naverAccessToken = naverTempTokenRedisRepository.findNaverAccessToken(naverTempToken);
        if (naverAccessToken == null) {
            throw new BusinessException(ErrorCode.NAVER_TEMP_TOKEN_EXPIRED);
        }

        NaverUserInfoResponse naverUser = naverOAuthClient.fetchUserInfo(naverAccessToken);
        String providerId = naverUser.getProviderId();

        if (memberQueryService.existsSocialAccount(SocialProvider.NAVER, providerId)) {
            throw new BusinessException(ErrorCode.SOCIAL_ACCOUNT_ALREADY_REGISTERED);
        }

        Member savedMember = memberCommandService.signUpSocial(
            username, nickname, fullName, gender, birthDate, phoneNumber,
            pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled, referrerNickname
        );

        memberCommandService.saveSocialAccount(
            MemberSocialAccount.of(
                savedMember.getId(), SocialProvider.NAVER, providerId,
                naverUser.getEmail(), naverUser.getNickname(), naverUser.getProfileImageUrl()
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

    private JwtResponse issueJwt(Member member) {
        return tokenService.issue(member, false);
    }
}
