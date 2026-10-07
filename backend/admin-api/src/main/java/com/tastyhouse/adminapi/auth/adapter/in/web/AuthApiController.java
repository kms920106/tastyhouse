package com.tastyhouse.adminapi.auth.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.application.auth.port.in.AdminAuthLoginCommand;
import com.tastyhouse.application.auth.port.in.AdminLoginUseCase;
import com.tastyhouse.application.auth.port.in.AdminLogoutUseCase;
import com.tastyhouse.application.auth.port.in.AdminTokenRefreshUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.ratelimit.RateLimit;
import com.tastyhouse.apicommon.ratelimit.RateLimitKeyType;
import com.tastyhouse.adminapi.auth.adapter.in.web.request.LoginRequest;
import com.tastyhouse.adminapi.auth.adapter.in.web.request.RefreshTokenRequest;
import com.tastyhouse.adminapi.auth.adapter.in.web.response.JwtResponse;

@Tag(name = "Admin Auth", description = "관리자 인증 API")
@RestController
@RequestMapping("/api/auth")
class AuthApiController {

    private final AdminLoginUseCase adminLoginUseCase;
    private final AdminTokenRefreshUseCase adminTokenRefreshUseCase;
    private final AdminLogoutUseCase adminLogoutUseCase;

    public AuthApiController(
        AdminLoginUseCase adminLoginUseCase,
        AdminTokenRefreshUseCase adminTokenRefreshUseCase,
        AdminLogoutUseCase adminLogoutUseCase
    ) {
        this.adminLoginUseCase = adminLoginUseCase;
        this.adminTokenRefreshUseCase = adminTokenRefreshUseCase;
        this.adminLogoutUseCase = adminLogoutUseCase;
    }

    @Operation(summary = "관리자 로그인", description = "아이디/비밀번호 인증 후 JWT(Access/Refresh)를 발급합니다.")
    @RateLimit(limit = 10, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:admin_login")
    @PostMapping("/v1/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@Valid @RequestBody LoginRequest request) {
        AdminAuthLoginCommand command = request.toCommand();
        return ResponseEntity.ok(ApiResponse.success(JwtResponse.from(adminLoginUseCase.login(command))));
    }

    @Operation(summary = "토큰 갱신", description = "Refresh Token으로 새로운 Access/Refresh Token을 발급합니다.")
    @PostMapping("/v1/refresh")
    public ResponseEntity<ApiResponse<JwtResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.success(JwtResponse.from(adminTokenRefreshUseCase.refresh(request.refreshToken()))));
    }

    @Operation(summary = "로그아웃", description = "Access Token을 블랙리스트에 등록하고 Refresh Token을 삭제합니다.")
    @PostMapping("/v1/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String bearerToken) {
        adminLogoutUseCase.logout(bearerToken);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
