package com.tastyhouse.external.oauth.spi;

/**
 * 소셜 로그인 제공자 연동 SPI.
 *
 * <p>web-api는 이 인터페이스와 그 값 타입({@link SocialProfile}·{@link SocialCredential} 등)만 알고,
 * 제공자별 wire DTO({@code KakaoUserInfoResponse}·{@code AppleIdTokenPayload} 등)와 HTTP 호출 방식은
 * 알지 않는다. 그래서 제공자 API 응답 스키마가 바뀌어도 파장이 이 모듈 안에서 멈춘다.
 *
 * <p><b>이 포트를 domain-module에 두지 않은 이유</b>: domain-module의 출력 포트
 * ({@code MailSender}·{@code FileStoragePort} 등)는 전부 <i>도메인 서비스가</i> 불변식을 만족시키려고
 * 호출하는 것들이다. 소셜 OAuth는 호출부가 전부 web-api(표현 계층)이고 도메인 서비스가 쓰는 곳이 없어,
 * domain-module에 두면 "아무 도메인 서비스도 호출하지 않는 포트"가 된다. 도메인에 대응 개념이 없는
 * 공유 기술은 별도 모듈이 소유한다는 모듈 경계 규칙(security-module의 Redis JWT·rate limit 선례)에 따라
 * 어댑터와 같은 모듈(external-api)이 자신의 SPI를 소유한다.
 *
 * <p>제공자별 흐름 차이는 두 단계로 흡수한다 — 카카오·네이버·애플의 토큰 교환과 페이스북의 토큰 검증은
 * 모두 {@link #exchange}, 카카오·네이버·페이스북의 userinfo 조회와 애플의 id_token 검증·추출은 모두
 * {@link #fetchProfile}이다.
 */
public interface SocialOAuthClient {

    /**
     * 이 어댑터가 담당하는 제공자. 소비 측이 {@code List<SocialOAuthClient>}를 주입받아 제공자별로
     * 라우팅할 때 키로 쓴다.
     */
    SocialProvider provider();

    /**
     * 클라이언트가 넘긴 원본 자격증명을 프로필 조회에 재사용 가능한 자격증명으로 바꾼다.
     *
     * <p>카카오·네이버·애플은 인가 코드를 토큰으로 교환하고, 페이스북은 이미 액세스 토큰을 받았으므로
     * app_id 검증만 수행한 뒤 그대로 돌려준다. 검증 실패 시 {@code BusinessException}을 던진다.
     */
    SocialCredential exchange(SocialAuthorization authorization);

    /**
     * 자격증명으로 제공자 프로필을 조회한다.
     *
     * <p>애플은 여기서 JWKS를 네트워크로 받아 id_token 서명을 매번 재검증한다(값싼 조회가 아니다).
     */
    SocialProfile fetchProfile(SocialCredential credential);
}
