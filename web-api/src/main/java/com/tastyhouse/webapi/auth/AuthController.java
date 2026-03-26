package com.tastyhouse.webapi.auth;

import com.tastyhouse.core.common.CommonResponse;
import com.tastyhouse.webapi.auth.request.LoginRequest;
import com.tastyhouse.webapi.auth.request.RefreshTokenRequest;
import com.tastyhouse.webapi.auth.request.SignUpRequest;
import com.tastyhouse.webapi.auth.response.JwtResponse;
import com.tastyhouse.webapi.member.MemberService;
import com.tastyhouse.webapi.ratelimit.RateLimit;
import com.tastyhouse.webapi.ratelimit.RateLimitKeyType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final MemberService memberService;

    @Operation(summary = "회원가입", description = "새 회원을 등록합니다. 휴대폰번호 입력 시 SMS 인증(phoneVerifyToken)이 필요합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "회원가입 성공", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "400", description = "유효성 검증 실패 또는 인증 토큰 오류", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "409", description = "아이디 또는 닉네임 중복", content = @Content(schema = @Schema(hidden = true)))
    })
    @RateLimit(limit = 10, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:signup")
    @PostMapping("/signup")
    public ResponseEntity<CommonResponse<Void>> signUp(@Valid @RequestBody SignUpRequest request) {
        memberService.signUp(
            request.username(),
            request.password(),
            request.nickname(),
            request.fullName(),
            request.gender(),
            request.birthDate(),
            request.phoneNumber(),
            request.pushNotificationEnabled(),
            request.marketingInfoEnabled(),
            request.eventInfoEnabled(),
            request.phoneVerifyToken(),
            request.emailVerifyToken(),
            request.referrerNickname()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success(null));
    }

    @Operation(summary = "로그인", description = "사용자 인증을 통해 JWT 토큰을 발급합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그인 성공", content = @Content(schema = @Schema(implementation = JwtResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패 (아이디 또는 비밀번호 불일치)", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "429", description = "요청 횟수 초과 (IP당 분당 10회)", content = @Content(schema = @Schema(hidden = true)))
    })
    @RateLimit(limit = 10, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:login")
    @PostMapping("/login")
    public ResponseEntity<CommonResponse<JwtResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(CommonResponse.success(
            authService.login(loginRequest.username(), loginRequest.password(), loginRequest.rememberMe())));
    }

    @Operation(summary = "로그아웃", description = "Access Token을 블랙리스트에 등록하고 Refresh Token을 삭제하여 로그아웃 처리합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그아웃 성공", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "400", description = "유효하지 않은 토큰", content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/logout")
    public ResponseEntity<CommonResponse<Void>> logout(@RequestHeader("Authorization") String bearerToken) {
        authService.logout(bearerToken);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "토큰 갱신", description = "Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "토큰 갱신 성공", content = @Content(schema = @Schema(implementation = JwtResponse.class))),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token", content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/refresh")
    public ResponseEntity<CommonResponse<JwtResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(CommonResponse.success(authService.refresh(request.refreshToken())));
    }
}
