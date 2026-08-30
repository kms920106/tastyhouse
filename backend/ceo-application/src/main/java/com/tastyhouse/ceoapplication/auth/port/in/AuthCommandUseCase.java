package com.tastyhouse.ceoapplication.auth.port.in;

import com.tastyhouse.ceoapplication.auth.response.JwtResponse;

/**
 * 점주 인증 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code AuthCommandService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 *
 * <p>인증은 로그인·토큰 발급/폐기 등 상태를 바꾸는 연산만 있어 QueryUseCase 짝을 두지 않는다.
 * 서블릿/시큐리티 타입은 이 계약에 등장하지 않는다 — 컨트롤러가 원시값(Bearer 토큰 문자열·클라이언트
 * IP·User-Agent)을 추출해 넘기고, 시큐리티 컨텍스트 조작은 구현이 담당한다.
 *
 * <p>{@code refresh}·{@code logout}은 단일 문자열 인자뿐이라 Command로 묶지 않는다(web-api·admin-api의
 * {@code AuthCommandUseCase} 선례와 동일). 이름 있는 record로 묶어 얻는 이득 — 같은 타입 인자의 순서
 * 착각 방지 — 이 인자 1개에는 존재하지 않는다.
 */
public interface AuthCommandUseCase {

    JwtResponse login(AuthLoginCommand command);

    JwtResponse refresh(String refreshToken);

    void logout(String bearerToken);
}
