package com.tastyhouse.webapi.auth.apple;

import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
import com.tastyhouse.external.oauth.apple.AppleIdTokenPayload;
import com.tastyhouse.external.oauth.apple.AppleOAuthClient;
import com.tastyhouse.external.oauth.apple.AppleTokenResponse;
import com.tastyhouse.webapi.auth.response.JwtResponse;
import com.tastyhouse.webapi.auth.response.SocialLinkResponse;
import com.tastyhouse.webapi.auth.response.SocialLoginResponse;
import com.tastyhouse.webapi.auth.response.SocialProfile;
import com.tastyhouse.webapi.config.jwt.JwtTokenProvider;
import com.tastyhouse.webapi.config.jwt.repository.AppleTempTokenRedisRepository;
import com.tastyhouse.webapi.config.jwt.service.TokenService;

@Service
@RequiredArgsConstructor
public class AppleSocialLoginService {

    private final AppleOAuthClient appleOAuthClient;
    private final MemberCommandService memberCommandService;
    private final MemberQueryService memberQueryService;
    private final TokenService tokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AppleTempTokenRedisRepository appleTempTokenRedisRepository;

    // 인가 코드로 Apple 로그인 처리
    // - 인가 코드 → Apple token 교환 → id_token 검증 및 사용자 식별
    // - 기존 회원: JWT 발급
    // - 신규 사용자: appleTempToken 반환 (NEEDS_SIGN_UP)
    // - 동일 이메일 일반가입 계정 존재: appleTempToken 반환 (NEEDS_LINKING)
    //
    // [Apple 특이점] 사용자 이름(name)은 최초 동의 시에만 form_post로 전달되며 id_token에 포함되지 않는다.
    // 따라서 Apple 프로필에는 sub/email만 저장하고, 회원가입 시 사용자가 직접 이름을 입력한다.
    @Transactional
    public SocialLoginResponse login(String authorizationCode) {
        AppleTokenResponse appleToken = appleOAuthClient.fetchToken(authorizationCode);

        AppleIdTokenPayload appleUser;
        try {
            appleUser = appleOAuthClient.verifyAndExtractIdToken(appleToken.idToken());
        } catch (RuntimeException e) {
            throw new BusinessException(ErrorCode.APPLE_ID_TOKEN_INVALID);
        }

        String providerId = appleUser.sub();

        Optional<MemberSocialAccount> socialAccountOpt =
            memberQueryService.findSocialAccount(SocialProvider.APPLE, providerId);

        if (socialAccountOpt.isPresent()) {
            MemberSocialAccount socialAccount = socialAccountOpt.get();
            // Apple은 이메일 외 nickname/profileImageUrl 미제공 → email만 업데이트
            socialAccount.updateProviderInfo(appleUser.email(), null, null);

            Member member = memberQueryService.getById(new MemberId(socialAccount.getMemberId()));
            return SocialLoginResponse.ofLogin(issueJwt(member));
        }

        // 소셜 계정은 없지만 동일 이메일로 일반가입한 회원이 존재하는 경우
        // → 사용자 동의 후 연동 처리가 필요하므로 NEEDS_LINKING 반환
        String appleEmail = appleUser.email();
        if (StringUtils.hasText(appleEmail) && memberQueryService.existsByUsername(appleEmail)) {
            String appleTempToken = issueTempToken(appleToken.idToken());
            return SocialLoginResponse.ofLinkingRequired(appleTempToken);
        }

        String appleTempToken = issueTempToken(appleToken.idToken());
        return SocialLoginResponse.ofSignUpRequired(appleTempToken);
    }

    // Apple 계정을 기존 일반가입 계정에 연동하고 JWT 발급
    // - phoneVerifyToken으로 본인 확인 (전화번호로 Member 조회)
    // - appleTempToken으로 Redis에서 appleIdToken 조회 후 사용자 정보 추출
    // - 전화번호로 가입된 회원이 없으면 NEEDS_SIGN_UP 반환 (appleTempToken 유지)
    // - MEMBER_SOCIAL_ACCOUNT INSERT 후 JWT 발급 (appleTempToken 삭제)
    @Transactional
    public SocialLinkResponse linkAccount(String appleTempToken, String phoneVerifyToken) {
        if (!jwtTokenProvider.validatePhoneVerifyToken(phoneVerifyToken)) {
            throw new BusinessException(ErrorCode.MEMBER_PHONE_AUTH_EXPIRED);
        }

        String appleIdToken = appleTempTokenRedisRepository.findAppleIdToken(appleTempToken);
        if (appleIdToken == null) {
            throw new BusinessException(ErrorCode.APPLE_TEMP_TOKEN_EXPIRED);
        }

        AppleIdTokenPayload appleUser;
        try {
            appleUser = appleOAuthClient.verifyAndExtractIdToken(appleIdToken);
        } catch (RuntimeException e) {
            throw new BusinessException(ErrorCode.APPLE_ID_TOKEN_INVALID);
        }

        String providerId = appleUser.sub();

        // 이미 Apple 소셜 계정이 연동된 경우 중복 연동을 방지한다.
        if (memberQueryService.existsSocialAccount(SocialProvider.APPLE, providerId)) {
            throw new BusinessException(ErrorCode.SOCIAL_ACCOUNT_ALREADY_REGISTERED);
        }

        String phoneNumber = jwtTokenProvider.getPhoneNumberFromPhoneVerifyToken(phoneVerifyToken);
        Optional<Member> findMember = memberQueryService.findByPhoneNumberAndStatusNot(phoneNumber, MemberStatus.DELETED);

        // 해당 전화번호로 가입된 회원이 없으면 회원가입이 필요한 상태로 응답한다.
        // appleTempToken은 /signup/apple에서 재사용해야 하므로 삭제하지 않는다.
        if (findMember.isEmpty()) {
            return SocialLinkResponse.ofSignUpRequired(
                appleTempToken,
                new SocialProfile(
                    providerId,
                    appleUser.email(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            );
        }

        Member member = findMember.get();

        memberCommandService.saveSocialAccount(
            MemberSocialAccount.of(
                member.getId(),
                SocialProvider.APPLE,
                providerId,
                appleUser.email(),
                null,
                null
            )
        );

        appleTempTokenRedisRepository.delete(appleTempToken);

        return SocialLinkResponse.ofLogin(issueJwt(member));
    }

    // Apple 소셜 회원가입 처리 후 JWT 발급
    // - appleTempToken으로 Redis에서 appleIdToken 조회
    // - 회원가입 완료 후 appleTempToken 삭제 (1회용)
    @Transactional
    public JwtResponse signUp(
        String appleTempToken,
        String username,
        String nickname,
        String fullName,
        Gender gender,
        Integer birthDate,
        String phoneNumber,
        boolean pushNotificationEnabled,
        boolean marketingInfoEnabled,
        boolean eventInfoEnabled,
        String referrerNickname
    ) {
        String appleIdToken = appleTempTokenRedisRepository.findAppleIdToken(appleTempToken);
        if (appleIdToken == null) {
            throw new BusinessException(ErrorCode.APPLE_TEMP_TOKEN_EXPIRED);
        }

        AppleIdTokenPayload appleUser;
        try {
            appleUser = appleOAuthClient.verifyAndExtractIdToken(appleIdToken);
        } catch (RuntimeException e) {
            throw new BusinessException(ErrorCode.APPLE_ID_TOKEN_INVALID);
        }

        String providerId = appleUser.sub();

        if (memberQueryService.existsSocialAccount(SocialProvider.APPLE, providerId)) {
            throw new BusinessException(ErrorCode.SOCIAL_ACCOUNT_ALREADY_REGISTERED);
        }

        Member savedMember = memberCommandService.signUpSocial(
            username, nickname, fullName, gender, birthDate, phoneNumber,
            pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled, referrerNickname
        );

        memberCommandService.saveSocialAccount(
            MemberSocialAccount.of(
                savedMember.getId(),
                SocialProvider.APPLE,
                providerId,
                appleUser.email(),
                null,
                null
            )
        );

        appleTempTokenRedisRepository.delete(appleTempToken);

        return issueJwt(savedMember);
    }

    private String issueTempToken(String appleIdToken) {
        String appleTempToken = UUID.randomUUID().toString();
        appleTempTokenRedisRepository.save(appleTempToken, appleIdToken);
        return appleTempToken;
    }

    private JwtResponse issueJwt(Member member) {
        return tokenService.issue(member, false);
    }
}
