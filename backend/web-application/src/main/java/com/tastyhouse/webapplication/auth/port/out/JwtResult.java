package com.tastyhouse.webapplication.auth.port.out;

/**
 * 회원 JWT 발급 결과 — Access/Refresh 토큰 쌍과 토큰 타입("Bearer").
 *
 * <p><b>챕터 10</b>에서 신설(챕터 06의 admin, 챕터 09의 ceo {@code JwtResult} 선례를 그대로 승계).
 * 인증은 Command 경로지만 토큰을 응답으로 되돌려주므로 다른 조회 경로와 같은 규칙을 적용한다 —
 * {@code AuthJwtResponse}(@Schema)는 web-api가 갖고, 유스케이스는 이 프레임워크-프리 record를 반환한다.
 * 대응하는 기존 {@code *Result}가 없어 새로 만들었으며, web만 소비하므로 web-application이 소유한다.
 *
 * <p>패키지 위치는 admin·ceo의 auth 계약 선례를 따라 앱 네임스페이스 아래 둔다 — 인증은 앱별로
 * 주체·시크릿·ErrorCode가 달라 공유 패키지({@code com.tastyhouse.application..})에 두지 않는다.
 */
public record JwtResult(
    String accessToken,
    String refreshToken,
    String tokenType
) {

    public static JwtResult of(
        String accessToken,
        String refreshToken,
        String tokenType
    ) {
        return new JwtResult(
            accessToken,
            refreshToken,
            tokenType
        );
    }
}
