package com.tastyhouse.application.auth.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.domain.member.model.MemberSocialAccount;
import com.tastyhouse.domain.member.model.MemberSocialProvider;
import com.tastyhouse.domain.member.model.MemberStatus;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.auth.port.out.MemberJwtResult;
import com.tastyhouse.application.auth.port.out.SocialAuthorization;
import com.tastyhouse.application.auth.port.out.SocialCredential;
import com.tastyhouse.application.auth.port.out.SocialLinkResult;
import com.tastyhouse.application.auth.port.out.SocialLoginResult;
import com.tastyhouse.application.auth.port.out.SocialOAuthClientPort;
import com.tastyhouse.application.auth.port.out.SocialOAuthResult;
import com.tastyhouse.application.auth.port.out.SocialProfile;
import com.tastyhouse.application.auth.port.out.SocialProvider;
import com.tastyhouse.application.auth.token.MemberJwtTokenProvider;
import com.tastyhouse.application.auth.token.MemberTokenService;
import com.tastyhouse.application.member.port.out.write.MemberLoadPort;
import com.tastyhouse.application.member.port.out.write.MemberSocialAccountLoadPort;
import com.tastyhouse.application.member.port.out.write.MemberSocialAccountSavePort;
import com.tastyhouse.application.member.service.MemberRegistrationService;
import com.tastyhouse.application.shared.exception.WebErrorCode;
import com.tastyhouse.security.token.SocialTempTokenProvider;
import com.tastyhouse.security.token.SocialTempTokenRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SocialLoginServiceTest {

    private static final MemberJwtResult JWT = MemberJwtResult.of("access", "refresh", "Bearer");

    private final Map<SocialProvider, ClientStub> clients = new HashMap<>();
    private FakeSocialTempTokenRepository tempTokens;
    private MemberRegistrationService memberRegistrationService;
    private MemberLoadPort memberLoadPort;
    private MemberSocialAccountLoadPort memberSocialAccountLoadPort;
    private MemberSocialAccountSavePort memberSocialAccountSavePort;
    private MemberJwtTokenProvider jwtTokenProvider;
    private SocialLoginService service;

    @BeforeEach
    void setUp() {
        for (SocialProvider provider : SocialProvider.values()) {
            clients.put(provider, new ClientStub(provider));
        }
        tempTokens = new FakeSocialTempTokenRepository();
        memberRegistrationService = mock(MemberRegistrationService.class);
        memberLoadPort = mock(MemberLoadPort.class);
        memberSocialAccountLoadPort = mock(MemberSocialAccountLoadPort.class);
        memberSocialAccountSavePort = mock(MemberSocialAccountSavePort.class);
        jwtTokenProvider = mock(MemberJwtTokenProvider.class);
        MemberTokenService tokenService = mock(MemberTokenService.class);
        when(tokenService.issue(any(Member.class), anyBoolean())).thenReturn(JWT);
        service = new SocialLoginService(
            new SocialOAuthClientRouter(new ArrayList<>(clients.values())),
            memberRegistrationService,
            memberLoadPort,
            memberSocialAccountLoadPort,
            memberSocialAccountSavePort,
            tokenService,
            jwtTokenProvider,
            tempTokens
        );
    }

    @Test
    @DisplayName("login: 이미 연결된 소셜 계정이면 provider 정보를 닉네임으로 갱신하고 JWT를 발급한다")
    void login_existingAccount_issuesJwt() {
        MemberSocialAccount account = MemberSocialAccount.of(MemberId.of(7L), MemberSocialProvider.KAKAO, "pid", null, null, null);
        when(memberSocialAccountLoadPort.findByProviderAndProviderId(MemberSocialProvider.KAKAO, "pid")).thenReturn(Optional.of(account));
        Member member7 = member(7L);
        when(memberLoadPort.findById(MemberId.of(7L))).thenReturn(Optional.of(member7));

        SocialLoginResult result = service.login(SocialProvider.KAKAO, SocialAuthorization.of("code"));

        assertThat(result.status()).isEqualTo(SocialLoginResult.Status.LOGIN);
        assertThat(result.jwt()).isEqualTo(JWT);
        assertThat(account.getProviderNickname()).isEqualTo("nickname");
        assertThat(tempTokens.saved).isEmpty();
    }

    @Test
    @DisplayName("login: 페이스북은 provider 닉네임 자리에 이름을 저장한다(SocialProfile.nickname이 없다)")
    void login_facebook_storesNameAsNickname() {
        MemberSocialAccount account = MemberSocialAccount.of(MemberId.of(7L), MemberSocialProvider.FACEBOOK, "pid", null, null, null);
        when(memberSocialAccountLoadPort.findByProviderAndProviderId(MemberSocialProvider.FACEBOOK, "pid")).thenReturn(Optional.of(account));
        Member member7 = member(7L);
        when(memberLoadPort.findById(MemberId.of(7L))).thenReturn(Optional.of(member7));

        service.login(SocialProvider.FACEBOOK, SocialAuthorization.of("access-token"));

        assertThat(account.getProviderNickname()).isEqualTo("name");
    }

    @Test
    @DisplayName("login: 같은 이메일 회원이 있으면 provider 임시토큰에 credential을 저장하고 연결을 요구한다")
    void login_emailExists_requiresLinking() {
        when(memberSocialAccountLoadPort.findByProviderAndProviderId(MemberSocialProvider.APPLE, "pid")).thenReturn(Optional.empty());
        when(memberLoadPort.existsByUsername("user@tastyhouse.com")).thenReturn(true);

        SocialLoginResult result = service.login(SocialProvider.APPLE, SocialAuthorization.of("code"));

        assertThat(result.status()).isEqualTo(SocialLoginResult.Status.NEEDS_LINKING);
        assertThat(tempTokens.saved).containsEntry(SocialTempTokenProvider.APPLE + ":" + result.tempToken(), "credential:code");
    }

    @Test
    @DisplayName("login: 신규 회원이면 가입을 요구하고, 네이버 state를 그대로 교환에 넘긴다")
    void login_newMember_requiresSignUp() {
        when(memberSocialAccountLoadPort.findByProviderAndProviderId(MemberSocialProvider.NAVER, "pid")).thenReturn(Optional.empty());

        SocialLoginResult result = service.login(SocialProvider.NAVER, SocialAuthorization.of("code", "state-1"));

        assertThat(result.status()).isEqualTo(SocialLoginResult.Status.NEEDS_SIGN_UP);
        assertThat(clients.get(SocialProvider.NAVER).lastAuthorization).isEqualTo(SocialAuthorization.of("code", "state-1"));
        assertThat(tempTokens.saved).containsEntry(SocialTempTokenProvider.NAVER + ":" + result.tempToken(), "credential:code");
    }

    @ParameterizedTest
    @CsvSource({
        "KAKAO, KAKAO_TEMP_TOKEN_EXPIRED",
        "NAVER, NAVER_TEMP_TOKEN_EXPIRED",
        "FACEBOOK, FACEBOOK_TEMP_TOKEN_EXPIRED",
        "APPLE, APPLE_TEMP_TOKEN_EXPIRED"
    })
    @DisplayName("linkAccount: 임시토큰이 만료되면 provider별 기존 에러코드로 거절한다")
    void linkAccount_expiredTempToken_isRejectedPerProvider(SocialProvider provider, WebErrorCode expected) {
        assertThatThrownBy(() -> service.linkAccount(provider, "missing", "sms-token"))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", expected);
    }

    @Test
    @DisplayName("linkAccount: 휴대폰 회원이 있으면 계정을 연결하고 임시토큰을 지운 뒤 JWT를 발급한다")
    void linkAccount_existingMember_linksAndIssuesJwt() {
        tempTokens.save(SocialTempTokenProvider.FACEBOOK, "temp", "stored-credential");
        when(memberSocialAccountLoadPort.existsByProviderAndProviderId(MemberSocialProvider.FACEBOOK, "pid")).thenReturn(false);
        when(jwtTokenProvider.getPhoneNumberFromSmsVerifyToken("sms-token")).thenReturn("01012345678");
        Member member9 = member(9L);
        when(memberLoadPort.findByPhoneNumberAndStatusNot("01012345678", MemberStatus.DELETED)).thenReturn(Optional.of(member9));

        SocialLinkResult result = service.linkAccount(SocialProvider.FACEBOOK, "temp", "sms-token");

        assertThat(result.status()).isEqualTo(SocialLinkResult.Status.LOGIN);
        assertThat(clients.get(SocialProvider.FACEBOOK).lastCredential).isEqualTo(SocialCredential.of("stored-credential"));
        ArgumentCaptor<MemberSocialAccount> captor = ArgumentCaptor.captor();
        verify(memberSocialAccountSavePort).save(captor.capture());
        assertThat(captor.getValue().getProvider()).isEqualTo(MemberSocialProvider.FACEBOOK);
        assertThat(captor.getValue().getProviderNickname()).isEqualTo("name");
        assertThat(tempTokens.saved).isEmpty();
    }

    @Test
    @DisplayName("linkAccount: 휴대폰 회원이 없으면 프로필과 함께 가입을 요구하고 임시토큰은 남긴다")
    void linkAccount_noMember_requiresSignUp() {
        tempTokens.save(SocialTempTokenProvider.KAKAO, "temp", "stored-credential");
        when(jwtTokenProvider.getPhoneNumberFromSmsVerifyToken("sms-token")).thenReturn("01012345678");
        when(memberLoadPort.findByPhoneNumberAndStatusNot("01012345678", MemberStatus.DELETED)).thenReturn(Optional.empty());

        SocialLinkResult result = service.linkAccount(SocialProvider.KAKAO, "temp", "sms-token");

        assertThat(result.status()).isEqualTo(SocialLinkResult.Status.NEEDS_SIGN_UP);
        assertThat(result.socialProfile().nickname()).isEqualTo("nickname");
        verify(memberSocialAccountSavePort, never()).save(any());
        assertThat(tempTokens.saved).containsKey(SocialTempTokenProvider.KAKAO + ":temp");
    }

    @Test
    @DisplayName("linkAccount: 휴대폰 인증 토큰이 유효하지 않으면 임시토큰을 조회하지 않고 거절한다")
    void linkAccount_invalidSmsToken_isRejected() {
        when(jwtTokenProvider.isInvalidSmsVerifyToken("bad")).thenReturn(true);

        assertThatThrownBy(() -> service.linkAccount(SocialProvider.KAKAO, "temp", "bad"))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", WebErrorCode.MEMBER_PHONE_AUTH_EXPIRED);
    }

    @Test
    @DisplayName("signUp: 회원을 등록하고 소셜 계정을 연결한 뒤 임시토큰을 지운다")
    void signUp_registersAndLinks() {
        tempTokens.save(SocialTempTokenProvider.APPLE, "temp", "id-token");
        Member member11 = member(11L);
        when(memberRegistrationService.signUpSocial(anyString(), anyString(), anyString(), any(), any(), anyString(),
            anyBoolean(), anyBoolean(), anyBoolean(), any())).thenReturn(member11);

        MemberJwtResult result = service.signUp(SocialProvider.APPLE, "temp", "user@tastyhouse.com", "nick", "홍길동",
            null, 19900101, "01012345678", true, false, false, null);

        assertThat(result).isEqualTo(JWT);
        ArgumentCaptor<MemberSocialAccount> captor = ArgumentCaptor.captor();
        verify(memberSocialAccountSavePort).save(captor.capture());
        assertThat(captor.getValue().getMemberId()).isEqualTo(MemberId.of(11L));
        assertThat(captor.getValue().getProvider()).isEqualTo(MemberSocialProvider.APPLE);
        assertThat(tempTokens.saved).isEmpty();
    }

    @Test
    @DisplayName("signUp: 이미 연결된 소셜 계정이면 회원을 등록하지 않고 거절한다")
    void signUp_alreadyRegistered_isRejected() {
        tempTokens.save(SocialTempTokenProvider.KAKAO, "temp", "credential");
        when(memberSocialAccountLoadPort.existsByProviderAndProviderId(MemberSocialProvider.KAKAO, "pid")).thenReturn(true);

        assertThatThrownBy(() -> service.signUp(SocialProvider.KAKAO, "temp", "user@tastyhouse.com", "nick", "홍길동",
            null, 19900101, "01012345678", true, false, false, null))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", WebErrorCode.SOCIAL_ACCOUNT_ALREADY_REGISTERED);
        verify(memberRegistrationService, never()).signUpSocial(any(), any(), any(), any(), any(), any(),
            anyBoolean(), anyBoolean(), anyBoolean(), any());
    }

    @Test
    @DisplayName("providerOf: 소셜 로그인을 지원하지 않는 provider 문자열은 SOCIAL_PROVIDER_TYPE_UNKNOWN으로 거절한다")
    void providerOf_unsupportedProvider_isRejected() {
        assertThat(SocialLoginService.providerOf("KAKAO")).isEqualTo(SocialProvider.KAKAO);
        assertThatThrownBy(() -> SocialLoginService.providerOf("GOOGLE"))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", DomainErrorCode.SOCIAL_PROVIDER_TYPE_UNKNOWN);
        assertThatThrownBy(() -> SocialLoginService.providerOf("LINE"))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", DomainErrorCode.SOCIAL_PROVIDER_TYPE_UNKNOWN);
    }

    private static Member member(long id) {
        Member member = mock(Member.class);
        when(member.getMemberId()).thenReturn(MemberId.of(id));
        return member;
    }

    private static final class ClientStub implements SocialOAuthClientPort {

        private final SocialProvider provider;
        private SocialAuthorization lastAuthorization;
        private SocialCredential lastCredential;

        private ClientStub(SocialProvider provider) {
            this.provider = provider;
        }

        @Override
        public SocialProvider provider() {
            return provider;
        }

        @Override
        public SocialOAuthResult<SocialCredential> exchange(SocialAuthorization authorization) {
            lastAuthorization = authorization;
            return SocialOAuthResult.success(SocialCredential.of("credential:" + authorization.code()));
        }

        @Override
        public SocialOAuthResult<SocialProfile> fetchProfile(SocialCredential credential) {
            lastCredential = credential;
            return SocialOAuthResult.success(new SocialProfile(
                "pid", "user@tastyhouse.com", "nickname", "image", "name", null, null, null, null, null));
        }
    }

    private static final class FakeSocialTempTokenRepository implements SocialTempTokenRepository {

        private final Map<String, String> saved = new HashMap<>();

        @Override
        public void save(SocialTempTokenProvider provider, String tempToken, String credential) {
            saved.put(provider + ":" + tempToken, credential);
        }

        @Override
        public String findCredential(SocialTempTokenProvider provider, String tempToken) {
            return saved.get(provider + ":" + tempToken);
        }

        @Override
        public void delete(SocialTempTokenProvider provider, String tempToken) {
            saved.remove(provider + ":" + tempToken);
        }
    }
}
