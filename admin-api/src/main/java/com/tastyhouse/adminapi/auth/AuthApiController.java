package com.tastyhouse.adminapi.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.security.ratelimit.RateLimit;
import com.tastyhouse.security.ratelimit.RateLimitKeyType;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.adminapi.auth.request.LoginRequest;
import com.tastyhouse.adminapi.auth.request.RefreshTokenRequest;
import com.tastyhouse.adminapi.auth.response.JwtResponse;

@Tag(name = "Admin Auth", description = "관리자 인증 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthApiController {

    private final AuthService authService;

    @Operation(summary = "관리자 로그인", description = "아이디/비밀번호 인증 후 JWT(Access/Refresh)를 발급합니다.")
    @RateLimit(limit = 10, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:admin_login")
    @PostMapping("/v1/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
            authService.login(request.username(), request.password(), request.rememberMe())
        ));
    }

    @Operation(summary = "토큰 갱신", description = "Refresh Token으로 새로운 Access/Refresh Token을 발급합니다.")
    @PostMapping("/v1/refresh")
    public ResponseEntity<ApiResponse<JwtResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.refresh(request.refreshToken())));
    }

    @Operation(summary = "로그아웃", description = "Access Token을 블랙리스트에 등록하고 Refresh Token을 삭제합니다.")
    @PostMapping("/v1/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String bearerToken) {
        authService.logout(bearerToken);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
