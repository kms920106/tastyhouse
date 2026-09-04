package com.tastyhouse.application.auth.service;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.tastyhouse.domain.ceo.model.Ceo;
import com.tastyhouse.domain.ceo.model.CeoLoginFailureReason;
import com.tastyhouse.domain.ceo.repository.CeoRepository;
import com.tastyhouse.application.ceo.port.in.CeoLoginHistoryCommandUseCase;
import com.tastyhouse.application.ceo.port.in.CeoLoginHistoryFailureCommand;
import com.tastyhouse.application.ceo.port.in.CeoLoginHistorySuccessCommand;
import com.tastyhouse.application.auth.token.CeoTokenService;
import com.tastyhouse.application.auth.security.CeoUserDetails;
import com.tastyhouse.application.auth.port.in.CeoAuthLoginCommand;
import com.tastyhouse.application.auth.port.out.CeoJwtResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 점주 로그인 접속기록 배선 봉인 테스트.
 *
 * <p>이 테스트가 지키는 것은 네 가지다.
 * <ul>
 *   <li>성공·실패 양쪽 모두 이력을 남긴다(실패 이력이 인증 예외와 함께 사라지지 않는다).</li>
 *   <li>실패 시 <b>원래 인증 예외가 그대로 rethrow</b>된다 — 응답 계약이 바뀌지 않는다.</li>
 *   <li>존재하지 않는 username은 기록하지 않는다(계정 존재 여부 탐색 표면 방지).</li>
 *   <li>기록 실패 시 정책이 성공·실패 경로에서 <b>의도적으로 비대칭</b>이다.</li>
 * </ul>
 */
class AuthCommandServiceTest {

    private static final String USERNAME = "ceo";
    private static final String PASSWORD = "password123!";
    private static final Long CEO_ID = 7L;
    private static final String IP = "121.130.11.24";
    private static final String USER_AGENT = "Mozilla/5.0";

    private AuthenticationManager authenticationManager;
    private CeoTokenService tokenService;
    private CeoRepository ceoRepository;
    private CeoLoginHistoryCommandUseCase ceoLoginHistoryCommandService;
    private CeoAuthCommandService authCommandService;

    @BeforeEach
    void setUp() {
        authenticationManager = mock(AuthenticationManager.class);
        tokenService = mock(CeoTokenService.class);
        ceoRepository = mock(CeoRepository.class);
        ceoLoginHistoryCommandService = mock(CeoLoginHistoryCommandUseCase.class);
        authCommandService = new CeoAuthCommandService(
            authenticationManager,
            tokenService,
            ceoRepository,
            ceoLoginHistoryCommandService
        );
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("로그인 성공 시 SUCCESS 이력을 남기고 토큰을 발급한다")
    void login_success_recordsSuccessHistory() {
        givenAuthenticationSucceeds();
        CeoJwtResult expected = CeoJwtResult.of("access", "refresh", "Bearer");
        when(tokenService.issue(any(Authentication.class), anyBoolean())).thenReturn(expected);

        CeoJwtResult actual = authCommandService.login(CeoAuthLoginCommand.of(USERNAME, PASSWORD, false, IP, USER_AGENT));

        assertThat(actual).isEqualTo(expected);
        verify(ceoLoginHistoryCommandService).recordSuccess(CeoLoginHistorySuccessCommand.of(CEO_ID, IP, USER_AGENT));
        verifyNoInteractions(ceoRepository);
    }

    @Test
    @DisplayName("비밀번호 불일치 시 BAD_CREDENTIALS 이력을 남기고 원래 예외를 rethrow한다")
    void login_badCredentials_recordsFailureAndRethrows() {
        BadCredentialsException authenticationException = new BadCredentialsException("bad credentials");
        givenAuthenticationFailsWith(authenticationException);
        givenCeoExists();

        assertThatThrownBy(() -> authCommandService.login(CeoAuthLoginCommand.of(USERNAME, PASSWORD, false, IP, USER_AGENT)))
            .isSameAs(authenticationException);

        verify(ceoLoginHistoryCommandService)
            .recordFailure(CeoLoginHistoryFailureCommand.of(
                CEO_ID, CeoLoginFailureReason.BAD_CREDENTIALS.name(), IP, USER_AGENT));
        verify(tokenService, never()).issue(any(), anyBoolean());
    }

    @Test
    @DisplayName("비활성 계정(DisabledException)은 ACCOUNT_INACTIVE 이력을 남긴다")
    void login_disabledAccount_recordsAccountInactive() {
        DisabledException authenticationException = new DisabledException("disabled");
        givenAuthenticationFailsWith(authenticationException);
        givenCeoExists();

        assertThatThrownBy(() -> authCommandService.login(CeoAuthLoginCommand.of(USERNAME, PASSWORD, false, IP, USER_AGENT)))
            .isSameAs(authenticationException);

        verify(ceoLoginHistoryCommandService)
            .recordFailure(CeoLoginHistoryFailureCommand.of(
                CEO_ID, CeoLoginFailureReason.ACCOUNT_INACTIVE.name(), IP, USER_AGENT));
    }

    @Test
    @DisplayName("잠긴 계정(LockedException)도 ACCOUNT_INACTIVE 이력을 남긴다")
    void login_lockedAccount_recordsAccountInactive() {
        LockedException authenticationException = new LockedException("locked");
        givenAuthenticationFailsWith(authenticationException);
        givenCeoExists();

        assertThatThrownBy(() -> authCommandService.login(CeoAuthLoginCommand.of(USERNAME, PASSWORD, false, IP, USER_AGENT)))
            .isSameAs(authenticationException);

        verify(ceoLoginHistoryCommandService)
            .recordFailure(CeoLoginHistoryFailureCommand.of(
                CEO_ID, CeoLoginFailureReason.ACCOUNT_INACTIVE.name(), IP, USER_AGENT));
    }

    @Test
    @DisplayName("존재하지 않는 username은 기록하지 않고 원래 예외를 rethrow한다")
    void login_unknownUsername_recordsNothing() {
        BadCredentialsException authenticationException = new BadCredentialsException("bad credentials");
        givenAuthenticationFailsWith(authenticationException);
        when(ceoRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authCommandService.login(CeoAuthLoginCommand.of(USERNAME, PASSWORD, false, IP, USER_AGENT)))
            .isSameAs(authenticationException);

        verifyNoInteractions(ceoLoginHistoryCommandService);
    }

    @Test
    @DisplayName("실패 경로의 기록 실패는 삼키고 원래 인증 예외를 rethrow한다 (비대칭 정책)")
    void login_failurePath_recordingFailureIsSwallowed() {
        BadCredentialsException authenticationException = new BadCredentialsException("bad credentials");
        givenAuthenticationFailsWith(authenticationException);
        givenCeoExists();
        doThrow(new IllegalStateException("DB 장애"))
            .when(ceoLoginHistoryCommandService)
            .recordFailure(argThat(command -> CEO_ID.equals(command.ceoId())));

        assertThatThrownBy(() -> authCommandService.login(CeoAuthLoginCommand.of(USERNAME, PASSWORD, false, IP, USER_AGENT)))
            .isSameAs(authenticationException);
    }

    @Test
    @DisplayName("성공 경로의 기록 실패는 전파한다 — 접속기록 없이 토큰을 발급하지 않는다 (비대칭 정책)")
    void login_successPath_recordingFailurePropagates() {
        givenAuthenticationSucceeds();
        IllegalStateException recordingFailure = new IllegalStateException("DB 장애");
        doThrow(recordingFailure)
            .when(ceoLoginHistoryCommandService)
            .recordSuccess(CeoLoginHistorySuccessCommand.of(CEO_ID, IP, USER_AGENT));

        assertThatThrownBy(() -> authCommandService.login(CeoAuthLoginCommand.of(USERNAME, PASSWORD, false, IP, USER_AGENT)))
            .isSameAs(recordingFailure);

        verify(tokenService, never()).issue(any(), anyBoolean());
    }

    @Test
    @DisplayName("refresh는 접속기록을 남기지 않는다 — 토큰 갱신은 새로운 접속이 아니다")
    void refresh_recordsNothing() {
        when(tokenService.refresh("refresh-token"))
            .thenReturn(CeoJwtResult.of("access", "refresh", "Bearer"));

        authCommandService.refresh("refresh-token");

        verifyNoInteractions(ceoLoginHistoryCommandService);
    }

    private void givenAuthenticationSucceeds() {
        CeoUserDetails userDetails = new CeoUserDetails(
            CEO_ID,
            USERNAME,
            List.of(new SimpleGrantedAuthority("ROLE_CEO"))
        );
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.getAuthorities()
        );
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
    }

    private void givenAuthenticationFailsWith(RuntimeException authenticationException) {
        when(authenticationManager.authenticate(any(Authentication.class)))
            .thenThrow(authenticationException);
    }

    private void givenCeoExists() {
        Ceo ceo = Ceo.reconstitute(CEO_ID, USERNAME, "encoded", "점주", null, null, null, null);
        when(ceoRepository.findByUsername(USERNAME)).thenReturn(Optional.of(ceo));
    }
}
