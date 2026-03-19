# 리팩토링 가이드

> 분석 기준일: 2026-03-20
> 대상: tastyhouse-api (Spring Boot 3.2.4, Java 21, Multi-module)

---

## HIGH - 즉시 수정 필요

### 1. JWT 토큰 타입 미검증 (토큰 혼용 공격 취약점)

**파일**: `web-api/.../config/jwt/JwtTokenProvider.java`

**문제**: Access Token과 Refresh Token이 동일한 구조로 생성되어 `type` 클레임이 없음. Refresh Token을 Access Token처럼 사용하거나 반대로 사용해도 구분 불가.

**개선**:
```java
// createToken에 tokenType 파라미터 추가
public String createToken(Authentication authentication, long expirationTime, String tokenType) {
    return Jwts.builder()
        .subject(authentication.getName())
        .claim("auth", authorities)
        .claim("type", tokenType)  // "ACCESS" 또는 "REFRESH"
        .issuedAt(now)
        .expiration(validity)
        .signWith(key)
        .compact();
}

public String createAccessToken(Authentication authentication) {
    return createToken(authentication, jwtProperties.getAccessTokenExpiration(), "ACCESS");
}

public String createRefreshToken(Authentication authentication, boolean rememberMe) {
    long ttl = rememberMe
        ? jwtProperties.getRememberMeRefreshTokenExpiration()
        : jwtProperties.getRefreshTokenExpiration();
    return createToken(authentication, ttl, "REFRESH");
}

// Refresh Token → Access Token 재발급 시 타입 검증
public String createAccessTokenFromRefreshToken(String refreshToken) {
    if (!validateToken(refreshToken)) throw new JwtException("Invalid refresh token");
    validateTokenType(refreshToken, "REFRESH");
    return createAccessToken(getAuthentication(refreshToken));
}

private void validateTokenType(String token, String expectedType) {
    Claims claims = parseClaims(token);
    String type = claims.get("type", String.class);
    if (!expectedType.equals(type)) {
        throw new JwtException("잘못된 토큰 타입. expected=" + expectedType + ", actual=" + type);
    }
}
```

---

### 2. 로그아웃 경로가 permitAll로 열려 있음

**파일**: `web-api/.../config/SecurityConfig.java`

**문제**: `/api/auth/**` 전체가 `permitAll()`이므로 `/api/auth/logout`도 비인증 요청 가능. 임의 Bearer Token을 블랙리스트에 등록할 수 있음.

**개선**:
```java
.authorizeHttpRequests(authorize -> authorize
    .requestMatchers("/api/auth/signup", "/api/auth/login", "/api/auth/refresh").permitAll()
    .requestMatchers("/api/auth/logout").authenticated()  // 인증 필요
    .requestMatchers(PublicPaths.PATTERNS).permitAll()
    .anyRequest().authenticated()
)
```

---

### 3. Member 엔티티의 무분별한 `@Setter` 사용

**파일**: `core-module/.../entity/user/Member.java`

**문제**: 클래스 레벨 `@Setter`로 `id`, `username`, `password` 등 불변 필드까지 외부에서 수정 가능. 도메인 무결성 보장 불가.

**개선**:
```java
@Getter
// @Setter 클래스 레벨 제거
@Entity
@Table(name = "MEMBER")
public class Member extends BaseEntity {

    // 필드는 동일 유지

    // 비즈니스 의미가 있는 도메인 메서드로 대체
    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void changeProfile(String statusMessage, Long profileImageFileId) {
        if (statusMessage != null) this.statusMessage = statusMessage;
        if (profileImageFileId != null) this.profileImageFileId = profileImageFileId;
    }

    public void updatePersonalInfo(String fullName, String phoneNumber, Integer birthDate,
                                    Gender gender, Boolean pushNotificationEnabled,
                                    Boolean marketingInfoEnabled, Boolean eventInfoEnabled) {
        if (fullName != null) this.fullName = fullName;
        if (phoneNumber != null) this.phoneNumber = phoneNumber;
        if (birthDate != null) this.birthDate = birthDate;
        if (gender != null) this.gender = gender;
        if (pushNotificationEnabled != null) this.pushNotificationEnabled = pushNotificationEnabled;
        if (marketingInfoEnabled != null) this.marketingInfoEnabled = marketingInfoEnabled;
        if (eventInfoEnabled != null) this.eventInfoEnabled = eventInfoEnabled;
    }

    public void deactivate() {
        this.memberStatus = MemberStatus.DELETED;
    }
}
```

---

### 4. 공개 경로가 SecurityConfig와 JwtAuthenticationFilter에 이중 하드코딩

**파일**: `SecurityConfig.java`, `JwtAuthenticationFilter.java`

**문제**: 공개 API 경로가 두 파일에 중복 관리됨. 한 곳만 수정하면 불일치 발생 → 보안 구멍.

**개선**:
```java
// 공통 상수 클래스 신규 생성: PublicPaths.java
public final class PublicPaths {

    private PublicPaths() {}

    public static final String[] PATTERNS = {
        "/api/auth/signup", "/api/auth/login", "/api/auth/refresh",
        "/api/policies/**", "/api/faqs/**", "/api/notices/**",
        "/api/banners/**", "/api/places/**", "/api/event/**",
        "/api/ranks/**", "/api/products/**",
        "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**"
    };

    private static final List<String> PREFIX_LIST = Arrays.stream(PATTERNS)
        .map(p -> p.replace("/**", "/").replace("**", ""))
        .toList();

    public static boolean isPublic(String requestUri) {
        return PREFIX_LIST.stream().anyMatch(requestUri::startsWith);
    }
}

// SecurityConfig에서 사용
.requestMatchers(PublicPaths.PATTERNS).permitAll()

// JwtAuthenticationFilter에서 사용
@Override
protected boolean shouldNotFilter(HttpServletRequest request) {
    return PublicPaths.isPublic(request.getRequestURI());
}
```

---

### 5. 결제 취소 시 포인트 환불 로직의 부분 실패 위험

**파일**: `web-api/.../payment/PaymentService.java`

**문제**:
- `memberPoint`가 `null`이면 포인트 환불을 묵시적으로 건너뜀 → 사용자 금전 손실
- `memberPointJpaRepository.findByMemberId(memberId)` 중복 호출 (2회)

**개선**:
```java
@Transactional
public PaymentCancelResponse cancelPayment(Long memberId, Long paymentId, PaymentCancelRequest request) {
    // ... 기존 취소 처리 로직 ...

    payment.cancel(request.cancelReason());
    order.cancel();

    // 포인트 처리 단일 메서드로 통합
    restorePoints(memberId, order);

    return PaymentCancelResponse.of(PaymentCancelCode.SUCCESS);
}

private void restorePoints(Long memberId, Order order) {
    if (order.getUsedPoint() <= 0 && order.getEarnedPoint() <= 0) return;

    // null이면 예외 발생 (묵시적 건너뜀 제거)
    MemberPoint memberPoint = memberPointJpaRepository.findByMemberId(memberId)
        .orElseThrow(() -> new EntityNotFoundException(ErrorCode.POINT_NOT_FOUND,
            "포인트 정보를 찾을 수 없습니다. memberId=" + memberId));

    if (order.getUsedPoint() > 0) {
        memberPoint.addPoints(order.getUsedPoint());
        memberPointHistoryJpaRepository.save(MemberPointHistory.builder()
            .memberId(memberId).pointType(PointType.REFUND)
            .pointAmount(order.getUsedPoint()).reason("결제 취소 환불")
            .build());
    }

    if (order.getEarnedPoint() > 0) {
        int deductAmount = Math.min(memberPoint.getAvailablePoints(), order.getEarnedPoint());
        memberPoint.deductPoints(deductAmount);
        memberPointHistoryJpaRepository.save(MemberPointHistory.builder()
            .memberId(memberId).pointType(PointType.USE)
            .pointAmount(-deductAmount).reason("결제 취소 적립금 회수")
            .build());
    }
}
```

---

## MEDIUM - 조기에 개선 권장

### 6. AuthController에 비즈니스 로직 직접 존재

**파일**: `web-api/.../auth/AuthController.java`

**문제**: 로그인, 로그아웃, 토큰 갱신 로직이 Controller에 직접 작성. 서비스 레이어 없이 재사용/테스트/트랜잭션 관리 불가.

**개선**: `AuthService` 신규 생성 후 Controller에서 위임.

```java
// AuthService.java (신규)
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    public JwtResponse login(String username, String password, boolean rememberMe) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(username, password));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtTokenProvider.createAccessToken(authentication);
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication, rememberMe);
        tokenRedisRepository.saveRefreshToken(
            authentication.getName(), refreshToken, jwtTokenProvider.getRefreshTokenTtl(rememberMe));

        return new JwtResponse(accessToken, refreshToken, "Bearer");
    }

    public void logout(String bearerToken) {
        String accessToken = extractToken(bearerToken);
        if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
            tokenBlacklist.add(accessToken, jwtTokenProvider.getExpirationMillis(accessToken));
            tokenRedisRepository.deleteRefreshToken(jwtTokenProvider.getUsernameFromJWT(accessToken));
        }
        SecurityContextHolder.clearContext();
    }

    public JwtResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken))
            throw new UnauthorizedException("유효하지 않은 Refresh Token입니다.");
        String username = jwtTokenProvider.getUsernameFromJWT(refreshToken);
        if (!tokenRedisRepository.isRefreshTokenValid(username, refreshToken))
            throw new UnauthorizedException("만료되었거나 이미 로그아웃된 Refresh Token입니다.");

        Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);
        String newAccessToken = jwtTokenProvider.createAccessToken(authentication);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(authentication);
        tokenRedisRepository.saveRefreshToken(
            username, newRefreshToken, jwtProperties.getRefreshTokenExpiration());

        return new JwtResponse(newAccessToken, newRefreshToken, "Bearer");
    }
}

// AuthController - 위임 방식으로 경량화
@PostMapping("/login")
public ResponseEntity<CommonResponse<JwtResponse>> authenticateUser(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(CommonResponse.success(
        authService.login(request.username(), request.password(), request.rememberMe())));
}
```

---

### 7. MemberService.signUp()의 파라미터 과다 (11개)

**파일**: `web-api/.../member/MemberService.java`

**문제**: `signUp` 메서드가 11개의 원시 파라미터를 받음. `SignUpRequest`를 서비스에 직접 전달하면 가독성 및 유지보수성 향상.

**개선**:
```java
@Transactional
public void signUp(SignUpRequest request) {
    if (!request.password().equals(request.passwordConfirm()))
        throw new BusinessException(ErrorCode.MEMBER_PASSWORD_CONFIRM_MISMATCH);

    if (memberJpaRepository.existsByUsername(request.username()))
        throw new BusinessException(ErrorCode.MEMBER_USERNAME_DUPLICATED);

    if (memberJpaRepository.existsByNickname(request.nickname()))
        throw new BusinessException(ErrorCode.MEMBER_NICKNAME_DUPLICATED);

    validatePhoneVerification(request.phoneNumber(), request.phoneVerifyToken());

    memberJpaRepository.save(Member.create(
        request.username(), passwordEncoder.encode(request.password()),
        request.nickname(), request.fullName(), request.gender(),
        request.birthDate(), request.phoneNumber(),
        request.marketingInfoEnabled(), request.eventInfoEnabled()
    ));
}
```

---

### 8. CommonResponse에 `@Data` 사용 - API 응답 객체의 가변성

**파일**: `core-module/.../common/CommonResponse.java`

**문제**: `@Data`는 Setter를 포함하여 API 응답 객체가 외부에서 변조 가능. `equals`/`hashCode`도 불필요하게 생성됨.

**개선**:
```java
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private PageInfo pagination;

    @Getter
    @AllArgsConstructor
    public static class PageInfo {
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
    }
    // 팩토리 메서드는 기존과 동일
}
```

---

### 9. 회원 미존재를 `ifPresent`로 무시 (사일런트 실패)

**파일**: `web-api/.../member/MemberService.java`

**문제**: `updateMemberProfile`, `updatePersonalInfo`에서 `findById().ifPresent()` 사용. 회원이 없어도 에러 없이 아무 일도 일어나지 않음 → 클라이언트는 성공으로 인식.

**개선**:
```java
@Transactional
public void updateMemberProfile(Long memberId, String nickname, String statusMessage, Long profileImageFileId) {
    Member member = memberJpaRepository.findById(memberId)
        .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
    member.changeProfile(nickname, statusMessage, profileImageFileId);
}

@Transactional
public void updatePersonalInfo(Long memberId, String fullName, String phoneNumber,
        Integer birthDate, Gender gender, Boolean pushNotificationEnabled,
        Boolean marketingInfoEnabled, Boolean eventInfoEnabled) {
    Member member = memberJpaRepository.findById(memberId)
        .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
    member.updatePersonalInfo(fullName, phoneNumber, birthDate, gender,
        pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled);
}
```

---

### 10. 페이지네이션 size 제한 없음 - DoS 위험

**파일**: `web-api/.../common/PageRequest.java`

**문제**: 클라이언트가 `size=999999`를 전달하면 서버에서 거대한 쿼리 실행.

**개선**:
```java
public record PageRequest(
    @Min(0) int page,
    @Min(1) @Max(100) int size
) {
    public PageRequest {
        if (size > 100) size = 100;
    }
}
```

---

## LOW - 품질 개선

### 11. JwtTokenProvider의 반복적 파싱 코드

**파일**: `web-api/.../config/jwt/JwtTokenProvider.java`

**문제**: `Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload()` 패턴이 8회 이상 반복.

**개선**:
```java
private Claims parseClaims(String token) {
    return Jwts.parser().verifyWith(key).build()
        .parseSignedClaims(token).getPayload();
}

// 모든 클레임 파싱 부분에서 parseClaims(token) 으로 대체
public String getUsernameFromJWT(String token) {
    return parseClaims(token).getSubject();
}
```

---

### 12. `processOnSitePaymentCompletion` 내 중복 isOnSitePayment 검사

**파일**: `web-api/.../payment/PaymentService.java`

**문제**: `completeOnSitePayment`에서 이미 `isOnSitePayment`를 검증 후 호출하는데, `processOnSitePaymentCompletion` 내부에서 동일 조건을 다시 검사.

**개선**: 내부 메서드의 중복 `if (isOnSitePayment(...))` 조건 제거.

---

### 13. 주문번호 생성 방식 스타일 불일치

**파일**: `web-api/.../order/OrderService.java`

**문제**: `generateOrderNumber()`는 UUID 8자리 사용, `generatePgOrderId()`는 `System.currentTimeMillis()` 사용. UUID 8자리는 충돌 가능성 존재.

**개선**:
```java
private String generateOrderNumber() {
    String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    return "ORD-" + dateTime + "-" + uuid;
}
```

---

## 우선순위 요약

| 순위 | 심각도 | 항목 |
|---:|:---:|------|
| 1 | HIGH | JWT 토큰 타입 미검증 - 토큰 혼용 공격 취약점 |
| 2 | HIGH | 로그아웃 경로 permitAll - 인증 우회 가능 |
| 3 | HIGH | Member `@Setter` 남용 - 도메인 무결성 파괴 |
| 4 | HIGH | 공개 경로 이중 관리 - 불일치 시 보안 구멍 |
| 5 | HIGH | 결제 취소 포인트 환불 누락 가능 - 금전 데이터 정합성 |
| 6 | MEDIUM | AuthController에 비즈니스 로직 - 테스트/재사용 불가 |
| 7 | MEDIUM | signUp 파라미터 과다 (11개) - 유지보수성 저하 |
| 8 | MEDIUM | CommonResponse `@Data` - API 응답 불변성 위반 |
| 9 | MEDIUM | ifPresent로 에러 무시 - 사일런트 실패 |
| 10 | MEDIUM | 페이지 size 무제한 - DoS 벡터 |
| 11 | LOW | JWT 파싱 코드 중복 |
| 12 | LOW | 중복 isOnSitePayment 검사 |
| 13 | LOW | 주문번호 생성 스타일 불일치 |
