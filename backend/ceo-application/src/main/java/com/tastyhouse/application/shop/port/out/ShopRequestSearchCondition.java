package com.tastyhouse.application.shop.port.out;

import java.time.LocalDate;

import com.tastyhouse.domain.shop.model.ShopRequestStatus;
import com.tastyhouse.domain.shop.model.ShopRequestType;

/**
 * 요청처리 현황 목록 조회 조건.
 *
 * <p>{@code shopId}만 필수다 — 소유권이 검증된 특정 가게의 요청만 조회하며 이 값이 인덱스 레인지 스캔의
 * 진입점이다. 나머지는 선택(null이면 전체)이다.
 *
 * <p>변경이력의 {@code retentionFrom}(6개월 하한)에 대응하는 필드가 <b>없는 것이 의도</b>다. 요청처리
 * 현황에는 조회 기간 상한을 두지 않으므로(근거는 {@code ShopRequestQueryService} Javadoc) 정책 하한을 실어
 * 보낼 것이 없다.
 */
public record ShopRequestSearchCondition(
    Long shopId,
    ShopRequestType requestType,
    ShopRequestStatus status,
    LocalDate startDate,
    LocalDate endDate
) {

    public static ShopRequestSearchCondition of(
        Long shopId,
        ShopRequestType requestType,
        ShopRequestStatus status,
        LocalDate startDate,
        LocalDate endDate
    ) {
        return new ShopRequestSearchCondition(
            shopId,
            requestType,
            status,
            startDate,
            endDate
        );
    }
}
