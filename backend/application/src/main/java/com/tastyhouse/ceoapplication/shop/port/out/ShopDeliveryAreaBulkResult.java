package com.tastyhouse.ceoapplication.shop.port.out;

/**
 * 배달가능지역 일괄 등록·반경 적용 결과.
 *
 * <p><b>챕터 09</b>에서 신설. 도메인 서비스가 돌려주는
 * {@code ShopDeliveryAreaService.BulkResult}는 도메인 <b>서비스</b>의 중첩 record라 api 모듈이 알 수
 * 없으므로({@code apiModuleShouldBeDomainModelFree}) 같은 네 값을 이 계약으로 옮겨 나른다.
 *
 * <p><b>거처는 이 앱의 네임스페이스({@code ceoapplication.shop.port.out})이고, 읽기 계약 패키지
 * ({@code com.tastyhouse.application..port.out})가 아니다.</b> 등록·반경 적용은 <b>Command 경로</b>의
 * 반환값이라 조회 계약이 아니며, 읽기 계약 패키지에 두면 {@code commandServicesShouldNotDependOnQueryDaos}
 * (CQRS 교차 주입 금지)가 CommandService의 반환 타입을 위반으로 잡는다. admin의 {@code JwtResult}가
 * {@code adminapplication.auth.port.out}에 있는 것과 같은 배치다.
 */
public record ShopDeliveryAreaBulkResult(
    int requestedCount,
    int addedCount,
    int skippedCount,
    int totalCount
) {
}
