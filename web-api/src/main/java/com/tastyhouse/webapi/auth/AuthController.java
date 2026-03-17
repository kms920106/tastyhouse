package com.tastyhouse.webapi.auth;

import com.tastyhouse.core.common.CommonResponse;
import com.tastyhouse.webapi.auth.request.LoginRequest;
import com.tastyhouse.webapi.auth.request.RefreshTokenRequest;
import com.tastyhouse.webapi.auth.request.SignUpRequest;
import com.tastyhouse.webapi.auth.response.JwtResponse;
import com.tastyhouse.webapi.member.MemberService;
import com.tastyhouse.webapi.config.jwt.JwtProperties;
import com.tastyhouse.webapi.config.jwt.JwtTokenProvider;
import com.tastyhouse.webapi.config.jwt.TokenBlacklist;
import com.tastyhouse.webapi.config.jwt.TokenRedisRepository;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
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

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final TokenBlacklist tokenBlacklist;
    private final TokenRedisRepository tokenRedisRepository;
    private final MemberService memberService;

    @Operation(summary = "회원가입", description = "새 회원을 등록합니다. 휴대폰번호 입력 시 SMS 인증(phoneVerifyToken)이 필요합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "회원가입 성공", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "400", description = "유효성 검증 실패 또는 비밀번호 불일치 / 인증 토큰 오류", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "409", description = "아이디 또는 닉네임 중복", content = @Content(schema = @Schema(hidden = true)))
    })
    @RateLimit(limit = 10, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:signup")
    @PostMapping("/signup")
    public ResponseEntity<CommonResponse<Void>> signUp(@Valid @RequestBody SignUpRequest request) {
        memberService.signUp(
            request.username(),
            request.password(),
            request.passwordConfirm(),
            request.nickname(),
            request.fullName(),
            request.gender(),
            request.birthDate(),
            request.phoneNumber(),
            request.marketingInfoEnabled(),
            request.eventInfoEnabled(),
            request.phoneVerifyToken()
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
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtTokenProvider.createAccessToken(authentication);
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication, loginRequest.rememberMe());

        // Refresh Token을 Redis에 저장 (rememberMe 여부에 따라 TTL 차등 적용)
        tokenRedisRepository.saveRefreshToken(
                authentication.getName(),
                refreshToken,
                jwtTokenProvider.getRefreshTokenTtl(loginRequest.rememberMe())
        );

        return ResponseEntity.ok(CommonResponse.success(new JwtResponse(accessToken, refreshToken, "Bearer")));
    }

    @Operation(summary = "로그아웃", description = "Access Token을 블랙리스트에 등록하고 Refresh Token을 삭제하여 로그아웃 처리합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그아웃 성공", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "400", description = "유효하지 않은 토큰", content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/logout")
    public ResponseEntity<CommonResponse<Void>> logout(@RequestHeader("Authorization") String bearerToken) {
        if (!StringUtils.hasText(bearerToken) || !bearerToken.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(CommonResponse.error("유효하지 않은 토큰입니다."));
        }
        String accessToken = bearerToken.substring(7).trim();
        if (jwtTokenProvider.validateToken(accessToken)) {
            // Access Token 블랙리스트 등록
            long expirationMillis = jwtTokenProvider.getExpirationMillis(accessToken);
            tokenBlacklist.add(accessToken, expirationMillis);

            // Refresh Token 삭제 (로그아웃 후 토큰 갱신 불가)
            String username = jwtTokenProvider.getUsernameFromJWT(accessToken);
            tokenRedisRepository.deleteRefreshToken(username);
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "토큰 갱신", description = "Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "토큰 갱신 성공", content = @Content(schema = @Schema(implementation = JwtResponse.class))),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token", content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/refresh")
    public ResponseEntity<CommonResponse<JwtResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(CommonResponse.error("유효하지 않은 Refresh Token입니다."));
        }

        String username = jwtTokenProvider.getUsernameFromJWT(refreshToken);

        // Redis에 저장된 Refresh Token과 일치하는지 검증 (탈취/로그아웃 여부 확인)
        if (!tokenRedisRepository.isRefreshTokenValid(username, refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(CommonResponse.error("만료되었거나 이미 로그아웃된 Refresh Token입니다."));
        }

        Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);
        String newAccessToken = jwtTokenProvider.createAccessToken(authentication);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(authentication);

        // 기존 Refresh Token을 새 토큰으로 교체 (Refresh Token Rotation)
        tokenRedisRepository.saveRefreshToken(
                username,
                newRefreshToken,
                jwtProperties.getRefreshTokenExpiration()
        );

        return ResponseEntity.ok(CommonResponse.success(new JwtResponse(newAccessToken, newRefreshToken, "Bearer")));
    }
}
