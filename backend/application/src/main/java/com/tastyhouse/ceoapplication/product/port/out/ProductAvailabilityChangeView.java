package com.tastyhouse.ceoapplication.product.port.out;

import java.util.List;

import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 메뉴 판매상태 일괄 변경 결과 — 성공한 메뉴 id와 실패 건별 사유.
 *
 * <p><b>챕터 09</b>에서 신설. 도메인 서비스가 돌려주는
 * {@code domain.product.service.ProductAvailabilityChangeResult}·{@code ProductAvailabilityFailure}는
 * 도메인 <b>서비스</b> 패키지의 타입이라 api 모듈이 알 수 없으므로
 * ({@code apiModuleShouldBeDomainModelFree}) 같은 값을 이 계약으로 옮겨 나른다.
 *
 * <p><b>거처는 이 앱의 네임스페이스({@code ceoapplication.product.port.out})이고, 읽기 계약 패키지
 * ({@code com.tastyhouse.application..port.out})가 아니다.</b> 판매상태 변경은 <b>Command 경로</b>의
 * 반환값이라 조회 계약이 아니며, 읽기 계약 패키지에 두면
 * {@code commandServicesShouldNotDependOnQueryDaos}(CQRS 교차 주입 금지)가 CommandService의 반환 타입을
 * 위반으로 잡는다(admin의 {@code JwtResult}가 {@code adminapplication.auth.port.out}에 있는 것과 같은 배치).
 *
 * <p>{@link ErrorCode}는 그대로 담는다 — 에러 계약은 <b>횡단 관심사</b>라 api 모듈에서도 참조가
 * 허용된 carve-out({@code domain.exception..})이고, 표현 계약이 코드·메시지를 꺼내 강등한다.
 */
public record ProductAvailabilityChangeView(
    List<Long> succeeded,
    List<Failure> failed
) {

    /** 판매상태를 바꾸지 못한 메뉴 한 건과 그 사유. */
    public record Failure(
        Long id,
        String name,
        ErrorCode errorCode
    ) {
    }
}
