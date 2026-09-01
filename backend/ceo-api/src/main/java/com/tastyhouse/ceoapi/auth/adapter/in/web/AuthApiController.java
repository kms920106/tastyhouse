package com.tastyhouse.ceoapi.auth.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.common.ClientIpResolver;
import com.tastyhouse.apicommon.ratelimit.RateLimit;
import com.tastyhouse.apicommon.ratelimit.RateLimitKeyType;
import com.tastyhouse.ceoapi.auth.adapter.in.web.request.LoginRequest;
import com.tastyhouse.ceoapi.auth.adapter.in.web.request.RefreshTokenRequest;
import com.tastyhouse.ceoapi.auth.adapter.in.web.response.JwtResponse;
import com.tastyhouse.ceoapplication.auth.port.in.AuthCommandUseCase;
import com.tastyhouse.ceoapplication.auth.port.in.AuthLoginCommand;

@Tag(name = "Ceo Auth", description = "점주 인증 API")
@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final AuthCommandUseCase authCommandUseCase;

    public AuthApiController(AuthCommandUseCase authCommandUseCase) {
        this.authCommandUseCase = authCommandUseCase;
    }

    /**
     * 점주 로그인. 성공·실패 모두 개인정보처리시스템 접속기록으로 남으므로, 서블릿 타입을 여기서 풀어
     * IP·User-Agent를 {@code String}으로 서비스에 넘긴다(서비스 계층의 web 의존 금지 경계).
     *
     * <p>{@code keyPrefix}를 개명하지 않는다 — Redis 카운터 키라서 바꾸면 배포 시점에 진행 중인 rate
     * limit 카운터가 전부 리셋된다.
     */
    @Operation(summary = "점주 로그인", description = "아이디/비밀번호 인증 후 JWT(Access/Refresh)를 발급합니다.")
    @RateLimit(limit = 10, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:ceo_login")
    @PostMapping("/v1/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(
        @Valid @RequestBody LoginRequest request,
        HttpServletRequest httpRequest
    ) {
        AuthLoginCommand command = request.toCommand(
            ClientIpResolver.resolve(httpRequest),
            httpRequest.getHeader("User-Agent")
        );
        return ResponseEntity.ok(ApiResponse.success(JwtResponse.from(authCommandUseCase.login(command))));
    }

    @Operation(summary = "토큰 갱신", description = "Refresh Token으로 새로운 Access/Refresh Token을 발급합니다.")
    @PostMapping("/v1/refresh")
    public ResponseEntity<ApiResponse<JwtResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.success(JwtResponse.from(authCommandUseCase.refresh(request.refreshToken()))));
    }

    @Operation(summary = "로그아웃", description = "Access Token을 블랙리스트에 등록하고 Refresh Token을 삭제합니다.")
    @PostMapping("/v1/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String bearerToken) {
        authCommandUseCase.logout(bearerToken);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
