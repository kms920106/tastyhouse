package com.tastyhouse.webapi.auth.apple;

import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
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
import com.tastyhouse.external.oauth.apple.AppleIdTokenPayload;
import com.tastyhouse.external.oauth.apple.AppleOAuthClient;
import com.tastyhouse.external.oauth.apple.AppleTokenResponse;
import com.tastyhouse.security.token.AppleTempTokenRedisRepository;
import com.tastyhouse.webapi.config.jwt.JwtTokenProvider;
import com.tastyhouse.webapi.config.jwt.service.TokenService;
import com.tastyhouse.webapi.member.service.MemberCommandService;
import com.tastyhouse.webapi.auth.response.AuthJwtResponse;
import com.tastyhouse.webapi.auth.response.AuthSocialLinkResponse;
import com.tastyhouse.webapi.auth.response.AuthSocialLoginResponse;
import com.tastyhouse.webapi.auth.response.AuthSocialProfileResponse;

@Service
@RequiredArgsConstructor
public class AppleSocialLoginService {

    private final AppleOAuthClient appleOAuthClient;
    private final MemberCommandService memberCommandService;
    private final MemberRepository memberRepository;
    private final MemberSocialAccountRepository memberSocialAccountRepository;
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
    public AuthSocialLoginResponse login(String authorizationCode) {
        AppleTokenResponse appleToken = appleOAuthClient.fetchToken(authorizationCode);

        AppleIdTokenPayload appleUser;
        try {
            appleUser = appleOAuthClient.verifyAndExtractIdToken(appleToken.idToken());
        } catch (RuntimeException e) {
            throw new BusinessException(ErrorCode.APPLE_ID_TOKEN_INVALID);
        }

        String providerId = appleUser.sub();

        Optional<MemberSocialAccount> socialAccountOpt =
            memberSocialAccountRepository.findByProviderAndProviderId(MemberSocialProvider.APPLE, providerId);

        if (socialAccountOpt.isPresent()) {
            MemberSocialAccount socialAccount = socialAccountOpt.get();
            // Apple은 이메일 외 nickname/profileImageUrl 미제공 → email만 업데이트
            socialAccount.updateProviderInfo(appleUser.email(), null, null);
            memberCommandService.saveSocialAccount(socialAccount);

            Member member = memberRepository.findById(socialAccount.getMemberId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
            return AuthSocialLoginResponse.ofLogin(issueJwt(member));
        }

        // 소셜 계정은 없지만 동일 이메일로 일반가입한 회원이 존재하는 경우
        // → 사용자 동의 후 연동 처리가 필요하므로 NEEDS_LINKING 반환
        String appleEmail = appleUser.email();
        if (StringUtils.hasText(appleEmail) && memberRepository.existsByUsername(appleEmail)) {
            String appleTempToken = issueTempToken(appleToken.idToken());
            return AuthSocialLoginResponse.ofLinkingRequired(appleTempToken);
        }

        String appleTempToken = issueTempToken(appleToken.idToken());
        return AuthSocialLoginResponse.ofSignUpRequired(appleTempToken);
    }

    // Apple 계정을 기존 일반가입 계정에 연동하고 JWT 발급
    // - smsVerifyToken으로 본인 확인 (전화번호로 Member 조회)
    // - appleTempToken으로 Redis에서 appleIdToken 조회 후 사용자 정보 추출
    // - 전화번호로 가입된 회원이 없으면 NEEDS_SIGN_UP 반환 (appleTempToken 유지)
    // - MEMBER_SOCIAL_ACCOUNT INSERT 후 JWT 발급 (appleTempToken 삭제)
    @Transactional
    public AuthSocialLinkResponse linkAccount(String appleTempToken, String smsVerifyToken) {
        if (!jwtTokenProvider.validateSmsVerifyToken(smsVerifyToken)) {
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
        if (memberSocialAccountRepository.existsByProviderAndProviderId(MemberSocialProvider.APPLE, providerId)) {
            throw new BusinessException(ErrorCode.SOCIAL_ACCOUNT_ALREADY_REGISTERED);
        }

        String phoneNumber = jwtTokenProvider.getPhoneNumberFromSmsVerifyToken(smsVerifyToken);
        Optional<Member> findMember = memberRepository.findByPhoneNumberAndStatusNot(phoneNumber, MemberStatus.DELETED);

        // 해당 전화번호로 가입된 회원이 없으면 회원가입이 필요한 상태로 응답한다.
        // appleTempToken은 /signup/apple에서 재사용해야 하므로 삭제하지 않는다.
        if (findMember.isEmpty()) {
            return AuthSocialLinkResponse.ofSignUpRequired(
                appleTempToken,
                new AuthSocialProfileResponse(
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
                member.getMemberId(),
                MemberSocialProvider.APPLE,
                providerId,
                appleUser.email(),
                null,
                null
            )
        );

        appleTempTokenRedisRepository.delete(appleTempToken);

        return AuthSocialLinkResponse.ofLogin(issueJwt(member));
    }

    // Apple 소셜 회원가입 처리 후 JWT 발급
    // - appleTempToken으로 Redis에서 appleIdToken 조회
    // - 회원가입 완료 후 appleTempToken 삭제 (1회용)
    @Transactional
    public AuthJwtResponse signUp(
        String appleTempToken,
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

        if (memberSocialAccountRepository.existsByProviderAndProviderId(MemberSocialProvider.APPLE, providerId)) {
            throw new BusinessException(ErrorCode.SOCIAL_ACCOUNT_ALREADY_REGISTERED);
        }

        Member savedMember = memberCommandService.signUpSocial(
            username, nickname, fullName, gender, birthDate, phoneNumber,
            pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled, referrerNickname
        );

        memberCommandService.saveSocialAccount(
            MemberSocialAccount.of(
                savedMember.getMemberId(),
                MemberSocialProvider.APPLE,
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

    private AuthJwtResponse issueJwt(Member member) {
        return tokenService.issue(member, false);
    }
}
