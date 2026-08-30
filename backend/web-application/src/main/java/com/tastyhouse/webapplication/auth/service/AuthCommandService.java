package com.tastyhouse.webapplication.auth.service;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.member.model.MemberGender;
import com.tastyhouse.domain.member.model.MemberSocialProvider;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.webapplication.auth.service.apple.AppleSocialLoginService;
import com.tastyhouse.webapplication.auth.service.facebook.FacebookSocialLoginService;
import com.tastyhouse.webapplication.auth.service.kakao.KakaoSocialLoginService;
import com.tastyhouse.webapplication.auth.service.naver.NaverSocialLoginService;
import com.tastyhouse.webapplication.auth.port.in.AuthCommandUseCase;
import com.tastyhouse.webapplication.auth.port.in.AuthSignUpCommand;
import com.tastyhouse.webapplication.auth.port.in.AuthSocialSignUpCommand;
import com.tastyhouse.webapplication.auth.response.AuthJwtResponse;
import com.tastyhouse.webapplication.auth.response.AuthPasswordResetTokenResponse;
import com.tastyhouse.webapplication.auth.response.AuthPhoneLoginResponse;
import com.tastyhouse.webapplication.auth.response.AuthSocialLinkResponse;
import com.tastyhouse.webapplication.auth.response.AuthSocialLoginResponse;

/**
 * 인증 컨트롤러 파사드.
 *
 * <p>실제 처리는 인증 수단별 하위 서비스({@link CredentialLoginService}·소셜 로그인 4종·
 * {@link PhoneLoginService}·{@link AuthPasswordResetService})가 담당하고, 이 클래스는 컨트롤러가 쓰는
 * 진입점과 HTTP 경계 타입 승격(예: {@code String gender} → {@code MemberGender})만 얇게 유지한다.
 *
 * <p><b>트랜잭션 원자성 판정: 묶을 대상 없음(파사드에 {@code @Transactional} 불필요).</b> 이 파사드의 모든
 * 메서드는 하위 서비스 <em>한 곳</em>에만 위임하는 1:1 pass-through이며, 한 요청이 여러 하위 서비스를
 * 순서대로 엮는 지점이 없다. 따라서 파사드 수준에서 쪼개지는 "검증→갱신 시퀀스"가 존재하지 않는다.
 *
 * <p>단계별 원자성 판정이 필요한 흐름은 그 흐름을 소유한 하위 서비스가 갖는다 — 회원가입은
 * {@link CredentialLoginService#signUp}(토큰 검증은 JWT 서명뿐이라 DB와 무관하고, DB write는 가입 한 번),
 * 비밀번호 재설정은 {@link AuthPasswordResetService}(클래스 Javadoc에 단계별 판정 명시)를 참고한다.
 * 로그인·토큰 갱신·로그아웃은 DB write가 없고 Redis 기반 토큰 저장소만 다루므로 DB 트랜잭션과 무관하다.
 */
@Service
public class AuthCommandService implements AuthCommandUseCase {

    private final CredentialLoginService credentialLoginService;
    private final AuthPasswordResetService authPasswordResetService;
    private final KakaoSocialLoginService kakaoSocialLoginService;
    private final NaverSocialLoginService naverSocialLoginService;
    private final FacebookSocialLoginService facebookSocialLoginService;
    private final AppleSocialLoginService appleSocialLoginService;
    private final PhoneLoginService phoneLoginService;

    public AuthCommandService(
        CredentialLoginService credentialLoginService,
        AuthPasswordResetService authPasswordResetService,
        KakaoSocialLoginService kakaoSocialLoginService,
        NaverSocialLoginService naverSocialLoginService,
        FacebookSocialLoginService facebookSocialLoginService,
        AppleSocialLoginService appleSocialLoginService,
        PhoneLoginService phoneLoginService
    ) {
        this.credentialLoginService = credentialLoginService;
        this.authPasswordResetService = authPasswordResetService;
        this.kakaoSocialLoginService = kakaoSocialLoginService;
        this.naverSocialLoginService = naverSocialLoginService;
        this.facebookSocialLoginService = facebookSocialLoginService;
        this.appleSocialLoginService = appleSocialLoginService;
        this.phoneLoginService = phoneLoginService;
    }

    // 회원가입 (생성된 회원 식별자를 반환)
    @Override
    public Long signUp(AuthSignUpCommand command) {
        return credentialLoginService.signUp(
            command.username(), command.password(), command.nickname(), command.fullName(),
            MemberGender.from(command.gender()), command.birthDate(), command.phoneNumber(),
            command.pushNotificationEnabled(), command.marketingInfoEnabled(), command.eventInfoEnabled(),
            command.smsVerifyToken(), command.mailVerifyToken(), command.referrerNickname()
        );
    }

    // 로그인
    @Override
    public AuthJwtResponse login(String username, String password, boolean rememberMe) {
        return credentialLoginService.login(username, password, rememberMe);
    }

    // 토큰 갱신
    @Override
    public AuthJwtResponse refresh(String refreshToken) {
        return credentialLoginService.refresh(refreshToken);
    }

    // 로그아웃
    @Override
    public void logout(String bearerToken) {
        credentialLoginService.logout(bearerToken);
    }

    // 비밀번호 찾기 - 인증코드 발송
    @Override
    public void sendPasswordResetCode(String username) {
        authPasswordResetService.sendPasswordResetCode(username);
    }

    // 비밀번호 찾기 - 인증코드 확인
    @Override
    public AuthPasswordResetTokenResponse verifyPasswordResetCode(String username, String verificationCode) {
        return authPasswordResetService.verifyPasswordResetCode(username, verificationCode);
    }

    // 비밀번호 재설정
    @Override
    public void resetPassword(String passwordResetToken, String newPassword, String newPasswordConfirm) {
        authPasswordResetService.resetPassword(passwordResetToken, newPassword, newPasswordConfirm);
    }

    // 카카오 로그인 (기존 회원이면 JWT 발급, 신규이면 회원가입 필요 응답)
    @Override
    public AuthSocialLoginResponse kakaoLogin(String authorizationCode) {
        return kakaoSocialLoginService.login(authorizationCode);
    }

    // 휴대폰 인증 토큰으로 로그인 (기존 회원이면 JWT 발급, 신규이면 needsSignUp=true)
    @Override
    public AuthPhoneLoginResponse phoneLogin(String smsVerifyToken) {
        return phoneLoginService.login(smsVerifyToken);
    }

    // 소셜 계정을 기존 일반가입 계정에 연동 후 JWT 발급. 가입된 계정이 없으면 NEEDS_SIGN_UP 반환
    @Override
    public AuthSocialLinkResponse linkAccount(String provider, String tempToken, String smsVerifyToken) {
        return switch (MemberSocialProvider.from(provider)) {
            case KAKAO -> kakaoSocialLoginService.linkAccount(tempToken, smsVerifyToken);
            case NAVER -> naverSocialLoginService.linkAccount(tempToken, smsVerifyToken);
            case FACEBOOK -> facebookSocialLoginService.linkAccount(tempToken, smsVerifyToken);
            case APPLE -> appleSocialLoginService.linkAccount(tempToken, smsVerifyToken);
            default -> throw new BusinessException(ErrorCode.SOCIAL_PROVIDER_TYPE_UNKNOWN,
                ErrorCode.SOCIAL_PROVIDER_TYPE_UNKNOWN.getDefaultMessage() + ": " + provider);
        };
    }

    // 네이버 로그인 (기존 회원이면 JWT 발급, 신규이면 회원가입 필요 응답)
    @Override
    public AuthSocialLoginResponse naverLogin(String authorizationCode, String state) {
        return naverSocialLoginService.login(authorizationCode, state);
    }

    // 페이스북 로그인 (기존 회원이면 JWT 발급, 신규이면 회원가입 필요 응답)
    @Override
    public AuthSocialLoginResponse facebookLogin(String accessToken) {
        return facebookSocialLoginService.login(accessToken);
    }

    // 애플 로그인 (기존 회원이면 JWT 발급, 신규이면 회원가입 필요 응답)
    @Override
    public AuthSocialLoginResponse appleLogin(String authorizationCode) {
        return appleSocialLoginService.login(authorizationCode);
    }

    // 소셜 소셜 회원가입 후 JWT 발급
    @Override
    public AuthJwtResponse socialSignUp(AuthSocialSignUpCommand command) {
        MemberGender genderType = MemberGender.from(command.gender());
        return switch (MemberSocialProvider.from(command.provider())) {
            case KAKAO -> kakaoSocialLoginService.signUp(
                command.tempToken(), command.username(), command.nickname(), command.fullName(),
                genderType, command.birthDate(), command.phoneNumber(),
                command.pushNotificationEnabled(), command.marketingInfoEnabled(),
                command.eventInfoEnabled(), command.referrerNickname()
            );
            case NAVER -> naverSocialLoginService.signUp(
                command.tempToken(), command.username(), command.nickname(), command.fullName(),
                genderType, command.birthDate(), command.phoneNumber(),
                command.pushNotificationEnabled(), command.marketingInfoEnabled(),
                command.eventInfoEnabled(), command.referrerNickname()
            );
            case FACEBOOK -> facebookSocialLoginService.signUp(
                command.tempToken(), command.username(), command.nickname(), command.fullName(),
                genderType, command.birthDate(), command.phoneNumber(),
                command.pushNotificationEnabled(), command.marketingInfoEnabled(),
                command.eventInfoEnabled(), command.referrerNickname()
            );
            case APPLE -> appleSocialLoginService.signUp(
                command.tempToken(), command.username(), command.nickname(), command.fullName(),
                genderType, command.birthDate(), command.phoneNumber(),
                command.pushNotificationEnabled(), command.marketingInfoEnabled(),
                command.eventInfoEnabled(), command.referrerNickname()
            );
            default -> throw new BusinessException(ErrorCode.SOCIAL_PROVIDER_TYPE_UNKNOWN,
                ErrorCode.SOCIAL_PROVIDER_TYPE_UNKNOWN.getDefaultMessage() + ": " + command.provider());
        };
    }
}
