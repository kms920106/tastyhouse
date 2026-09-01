package com.tastyhouse.application.shop.port.out;

import com.tastyhouse.domain.shop.model.ShopRequestType;

/**
 * 요청 유형 카탈로그 항목 — 유형과 계약 변경 여부.
 *
 * <p><b>챕터 09</b>에서 신설. 사유는 {@link ShopRequestListItemViewResult}와 같다 —
 * {@code isContractAmending}은 읽기 accessor가 아닌 도메인 로직이라 api 모듈이 호출할 수 없다.
 */
public record ShopRequestTypeView(
    ShopRequestType requestType,
    boolean contractAmending
) {
}
