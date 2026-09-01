package com.tastyhouse.ceoapplication.shop.port.out;

/**
 * 배달가능지역 일괄 삭제 결과 — 실제로 삭제된 건수와 남은 전체 건수.
 *
 * <p><b>챕터 09</b>에서 신설. {@code removedCount}는 도메인 서비스가 직접 돌려주는 값이 아니라
 * "요청 건수 − 건너뛴 건수"로 유스케이스가 계산한 것이므로, 그 계산 결과를 담아 넘긴다.
 *
 * <p>거처가 읽기 계약 패키지가 아닌 이유는 {@link ShopDeliveryAreaBulkResult}와 같다 — Command 경로의
 * 반환값이다.
 */
public record ShopDeliveryAreaBulkDeleteResult(
    int removedCount,
    int totalCount
) {
}
