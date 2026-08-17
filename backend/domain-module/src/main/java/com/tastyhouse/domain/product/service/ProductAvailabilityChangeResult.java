package com.tastyhouse.domain.product.service;

import java.util.List;

/**
 * 일괄 품절·숨김 처리 결과. 부분 성공을 표현한다.
 *
 * <p>전체가 실패해도 예외를 던지지 않고 {@code failed}에 전량을 담아 반환한다 — "1건 실패"와 "전건 실패"의
 * 화면 처리가 실제로는 같으므로(성공 N건 / 실패 M건 안내), 응답 형태를 갈라놓으면 화면이 두 경로를
 * 타야 한다. 요청 자체가 잘못된 경우(빈 대상·기간 범위 위반·소유권 위반)만 예외다.
 *
 * @param succeeded 적용된 대상 id
 * @param failed    적용하지 못한 대상과 그 사유
 */
public record ProductAvailabilityChangeResult(
    List<Long> succeeded,
    List<ProductAvailabilityFailure> failed
) {

    public ProductAvailabilityChangeResult {
        succeeded = succeeded != null ? List.copyOf(succeeded) : List.of();
        failed = failed != null ? List.copyOf(failed) : List.of();
    }

    public static ProductAvailabilityChangeResult of(
        List<Long> succeeded,
        List<ProductAvailabilityFailure> failed
    ) {
        return new ProductAvailabilityChangeResult(
            succeeded,
            failed
        );
    }
}
