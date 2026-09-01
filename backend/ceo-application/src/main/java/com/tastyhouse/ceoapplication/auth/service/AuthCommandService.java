package com.tastyhouse.ceoapplication.auth.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.tastyhouse.ceoapplication.auth.port.in.AuthCommandUseCase;
import com.tastyhouse.ceoapplication.auth.port.in.AuthLoginCommand;
import com.tastyhouse.ceoapplication.auth.port.out.JwtResult;
import com.tastyhouse.ceoapplication.ceo.port.in.CeoLoginHistoryCommandUseCase;
import com.tastyhouse.ceoapplication.ceo.port.in.CeoLoginHistoryFailureCommand;
import com.tastyhouse.ceoapplication.ceo.port.in.CeoLoginHistorySuccessCommand;
import com.tastyhouse.ceoapplication.auth.token.TokenService;
import com.tastyhouse.ceoapplication.auth.security.CustomUserDetails;
import com.tastyhouse.domain.ceo.model.Ceo;
import com.tastyhouse.domain.ceo.model.CeoLoginFailureReason;
import com.tastyhouse.domain.ceo.repository.CeoRepository;

/**
 * 점주 인증(로그인/토큰갱신/로그아웃) 서비스.
 * 자격증명 검증은 Spring Security AuthenticationManager(+CeoUserDetailsService)에 위임한다.
 *
 * <p><b>이 클래스에 {@code @Transactional}을 붙이지 않는다.</b> 로그인 실패는 Spring Security 예외로
 * 전파되는데, 여기에 트랜잭션이 걸려 있으면 <b>실패 이력이 예외와 함께 롤백되어 영구히 남지 않는다.</b>
 * 기록은 {@link CeoLoginHistoryCommandUseCase}가 자기 트랜잭션으로 커밋한다.
 *
 * <p>서블릿 타입({@code HttpServletRequest})을 여기서 다루지 않고 컨트롤러가 IP·User-Agent를
 * {@code String}으로 뽑아 {@link AuthLoginCommand}에 실어 넘기는 이유: 챕터 04 물리 분리로 이 모듈
 * 전체에 ArchUnit {@code applicationMustBeServletFree}가 걸려 {@code jakarta.servlet..} 의존이 금지된다.
 * 이 모듈은 build.gradle에도 servlet 결합 타입이 없어 위반은 컴파일 단계에서 먼저 드러난다.
 *
 * <p>이 서비스와 {@code TokenService}는 web-api·admin-api의 동명 타입과 <b>의도적으로 중복</b>된다 —
 * 인증 주체가 {@code Ceo}이고 {@code CEO_*} ErrorCode와 {@code JWT_SECRET_CEO}를 쓰므로 통합하면
 * 앱별 인증 경계가 무너진다(backend/CLAUDE.md 앱별 중복 허용 목록).
 *
 * <p><b>기록 실패 시 정책은 성공·실패 경로가 의도적으로 비대칭이다.</b>
 * <ul>
 *   <li><b>성공 경로</b>: 기록 실패를 그대로 전파한다. 접속기록 없이 토큰이 발급되는 상태를 만들지
 *       않는다 — 개인정보처리시스템 접속기록은 법적 요구사항이므로, 남기지 못했다면 접속도 허용하지
 *       않는 편이 옳다.</li>
 *   <li><b>실패 경로</b>: 기록 실패를 catch·로깅하고 원래 인증 예외를 rethrow한다. 감사 쓰기 실패가
 *       인증 실패 응답 계약(401 {@code CEO_AUTHENTICATION_FAILED} 등)을 500으로 바꾸면 안 된다.</li>
 * </ul>
 * 이 비대칭은 {@code AuthCommandServiceTest}가 봉인한다.
 */
@Service
public class AuthCommandService implements AuthCommandUseCase {

    private static final Logger log = LoggerFactory.getLogger(AuthCommandService.class);

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final CeoRepository ceoRepository;
    private final CeoLoginHistoryCommandUseCase ceoLoginHistoryCommandUseCase;

    public AuthCommandService(
        AuthenticationManager authenticationManager,
        TokenService tokenService,
        CeoRepository ceoRepository,
        CeoLoginHistoryCommandUseCase ceoLoginHistoryCommandUseCase
    ) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.ceoRepository = ceoRepository;
        this.ceoLoginHistoryCommandUseCase = ceoLoginHistoryCommandUseCase;
    }

    /**
     * 아이디/비밀번호 인증 후 JWT 토큰을 발급하고, 성공·실패 양쪽 모두 로그인 이력을 남긴다.
     *
     * <p>점주가 로그인하면 주문자 이름·연락처·주소 같은 회원 개인정보를 열람할 수 있으므로, 로그인
     * 시점이 곧 개인정보처리시스템 접속 시점이다.
     *
     * <p>{@code command}의 {@code ipAddress}는 컨트롤러가 판별한 클라이언트 IP(판별 불가 시 null),
     * {@code userAgent}는 요청의 User-Agent다(미전송 시 null).
     */
    @Override
    public JwtResult login(AuthLoginCommand command) {
        String username = command.username();
        String ipAddress = command.ipAddress();
        String userAgent = command.userAgent();

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, command.password())
            );
        } catch (AuthenticationException e) {
            recordFailureQuietly(username, e, ipAddress, userAgent);
            throw e;
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        CeoLoginHistorySuccessCommand historyCommand =
            CeoLoginHistorySuccessCommand.of(userDetails.getCeoId(), ipAddress, userAgent);
        ceoLoginHistoryCommandUseCase.recordSuccess(historyCommand);

        return tokenService.issue(authentication, command.rememberMe());
    }

    /**
     * 리프레시 토큰으로 새 JWT 토큰을 재발급한다.
     *
     * <p>접속기록을 남기지 않는다 — 토큰 갱신은 새로운 개인정보 접속이 아니라 기존 세션의 연장이다.
     */
    @Override
    public JwtResult refresh(String refreshToken) {
        return tokenService.refresh(refreshToken);
    }

    /**
     * 토큰을 무효화하고 보안 컨텍스트를 초기화하여 로그아웃 처리한다.
     */
    @Override
    public void logout(String bearerToken) {
        tokenService.revoke(bearerToken);
        SecurityContextHolder.clearContext();
    }

    /**
     * 로그인 실패 이력을 남긴다. 기록에 실패해도 삼키고 원래 인증 예외가 그대로 전파되게 한다 —
     * 감사 쓰기 실패가 인증 실패 응답 계약을 바꾸면 안 된다(위 클래스 Javadoc의 비대칭 정책).
     */
    private void recordFailureQuietly(
        String username,
        AuthenticationException authenticationException,
        String ipAddress,
        String userAgent
    ) {
        try {
            Optional<Ceo> ceo = ceoRepository.findByUsername(username);
            if (ceo.isEmpty()) {
                // 존재하지 않는 아이디는 귀속할 점주가 없어 기록하지 않는다. 임의 username을 쌓으면
                // 계정 존재 여부를 탐색하는 표면이 된다.
                return;
            }
            CeoLoginFailureReason failureReason = resolveFailureReason(authenticationException);
            if (failureReason == null) {
                // 자격증명·계정상태 외의 예외는 로그인 시도의 결과로 분류할 수 없어 기록하지 않는다.
                return;
            }
            CeoLoginHistoryFailureCommand command = CeoLoginHistoryFailureCommand.of(
                ceo.get().getId(),
                failureReason.name(),
                ipAddress,
                userAgent
            );
            ceoLoginHistoryCommandUseCase.recordFailure(command);
        } catch (RuntimeException e) {
            log.error("점주 로그인 실패 이력 기록에 실패했습니다. username={}", username, e);
        }
    }

    /**
     * Spring Security 인증 예외를 실패 사유로 옮긴다. 분류할 수 없는 예외는 null을 반환해 기록하지
     * 않는다.
     */
    private CeoLoginFailureReason resolveFailureReason(AuthenticationException e) {
        if (e instanceof BadCredentialsException) {
            return CeoLoginFailureReason.BAD_CREDENTIALS;
        }
        if (e instanceof DisabledException || e instanceof LockedException) {
            return CeoLoginFailureReason.ACCOUNT_INACTIVE;
        }
        return null;
    }
}
