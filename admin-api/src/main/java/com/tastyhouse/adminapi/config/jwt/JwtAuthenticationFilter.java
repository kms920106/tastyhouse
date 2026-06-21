package com.tastyhouse.adminapi.config.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tastyhouse.adminapi.common.ApiResponse;
import com.tastyhouse.adminapi.config.jwt.service.TokenService;
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

/**
 * Access Token을 검증하여 SecurityContext에 인증을 주입하는 필터.
 * 토큰이 없으면 그냥 통과시키고, 인가(permitAll/authenticated)는 Spring Security가 최종 결정한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;
    private final ObjectMapper objectMapper;

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
                    writeUnauthorizedResponse(response, "유효하지 않거나 만료된 토큰입니다.");
                    return;
                }
            }
        } catch (Exception ex) {
            logger.error("Could not set admin authentication in security context", ex);
            writeUnauthorizedResponse(response, "인증에 실패했습니다.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void writeUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.error(message)));
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7).trim();
        }
        return null;
    }
}
