package com.tastyhouse.security.jwt;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.tastyhouse.security.token.BlacklistRedisRepository;

/**
 * Access Token을 검증해 SecurityContext에 인증을 주입하는 공용 필터.
 * 토큰이 없으면 그냥 통과시키고, 인가(permitAll/authenticated)는 Spring Security가 최종 결정한다.
 *
 * <p>{@code @Component}가 아니며(POJO), 각 API의 {@code JwtConfig}가 자신의 저장소 빈으로 등록한다.
 *
 * <p>[사용 금지] shouldNotFilter()로 공개 경로를 처리하면 안 되는 이유:
 * <ol>
 *   <li>경로 패턴이 HTTP 메서드를 구분하지 않아 PUT/DELETE 같은 인증 필요 요청도 필터가 skip됨.
 *       예) PublicPaths에 "/api/members/v1/*&#47;profile" 패턴이 있으면 GET(공개 조회)뿐 아니라
 *       PUT /api/members/v1/me/profile(인증 필요한 수정)도 skip되어 @CurrentUser가 null이 됨.</li>
 *   <li>인가(공개/비공개) 결정은 SecurityConfig의 authorizeHttpRequests에서 단일 관리해야 한다.
 *       shouldNotFilter는 보안 제어 수단이 아닌 성능 최적화 목적의 기능임.</li>
 *   <li>이 필터는 인증(Authentication)만 담당한다. 토큰이 없으면 다음 필터로 통과시키고,
 *       인가는 Spring Security의 AuthorizationFilter가 최종 결정한다.</li>
 * </ol>
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final BlacklistRedisRepository blacklistRepository;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                if (jwtTokenProvider.validateToken(jwt) && !blacklistRepository.contains(jwt)) {
                    jwtTokenProvider.validateTokenType(jwt, TokenType.ACCESS);

                    Authentication authentication = jwtTokenProvider.getAuthentication(jwt);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    writeUnauthorizedResponse(response, "유효하지 않거나 만료된 토큰입니다.");
                    return;
                }
            }
        } catch (Exception ex) {
            log.error("Could not set authentication in security context", ex);
            writeUnauthorizedResponse(response, "인증에 실패했습니다.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void writeUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, message);
        response.getWriter().write(objectMapper.writeValueAsString(problemDetail));
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7).trim();
        }
        return null;
    }
}
