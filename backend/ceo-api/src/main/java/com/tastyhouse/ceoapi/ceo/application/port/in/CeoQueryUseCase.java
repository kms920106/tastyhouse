package com.tastyhouse.ceoapi.ceo.application.port.in;

/**
 * 점주 계정 조회 인바운드 포트.
 *
 * <p>이 컨텍스트는 표현 목적 read model이 없어 컨트롤러가 주입하는 조회가 없다. 그럼에도 포트를
 * 두는 것은 {@code queryServicesShouldImplementUseCase}가 전 {@code *QueryService}를 매칭하기
 * 때문이며, 경계 타입만 싣는 연산 하나로 그 계약을 표현한다.
 *
 * <p>인증용 {@code findByUsername}은 {@code Optional<Ceo>}(도메인 모델)를 반환해 포트 패키지의
 * 경계 타입 규칙({@code commandRecordsShouldBeBoundaryTyped})에 걸리므로 구현 클래스에 남긴다 —
 * 호출자가 컨트롤러가 아니라 인증 컴포넌트(TokenService·CeoUserDetailsService·CeoSeeder)라
 * 인바운드 포트의 대상이 아니다.
 */
public interface CeoQueryUseCase {

    /**
     * @return 해당 아이디의 점주 계정이 이미 있으면 {@code true}
     */
    boolean existsByUsername(String username);
}
