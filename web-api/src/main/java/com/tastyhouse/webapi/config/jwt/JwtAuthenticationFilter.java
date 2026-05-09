package com.tastyhouse.webapi.config.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tastyhouse.core.common.CommonResponse;
import com.tastyhouse.webapi.config.jwt.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;
    private final ObjectMapper objectMapper;

    // [사용 금지] shouldNotFilter()로 공개 경로를 처리하면 안 되는 이유:
    // 1. 경로 패턴이 HTTP 메서드를 구분하지 않아 PUT/DELETE 같은 인증 필요 요청도 필터가 skip됨.
    //    예) PublicPaths에 "/api/members/v1/*/profile" 패턴이 있으면
    //        GET /api/members/v1/{id}/profile(공개 조회)뿐 아니라
    //        PUT /api/members/v1/me/profile(인증 필요한 수정)도 skip되어 @CurrentUser가 null이 됨.
    // 2. 인가(공개/비공개) 결정은 SecurityConfig의 authorizeHttpRequests에서 단일 관리해야 한다.
    //    shouldNotFilter는 보안 제어 수단이 아닌 성능 최적화 목적의 기능임.
    // 3. 이 필터는 인증(Authentication)만 담당한다. 토큰이 없으면 그냥 다음 필터로 통과시키고,
    //    인가(permitAll/authenticated)는 Spring Security의 AuthorizationFilter가 최종 결정한다.
    //
    // @Override
    // protected boolean shouldNotFilter(HttpServletRequest request) {
    //     return PublicPaths.isPublic(request.getRequestURI());
    // }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                if (jwtTokenProvider.validateToken(jwt) && !tokenService.isBlacklisted(jwt)) {
                    jwtTokenProvider.validateTokenType(jwt, TokenType.ACCESS);

                    Authentication authentication = jwtTokenProvider.getAuthentication(jwt);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    writeUnauthorizedResponse(response, "Invalid or expired token");
                    return;
                }
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
            writeUnauthorizedResponse(response, "Authentication failed");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void writeUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(CommonResponse.error(message)));
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7).trim();
        }
        return null;
    }
}
