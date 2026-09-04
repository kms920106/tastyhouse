package com.tastyhouse.application.shop.port.out;

import java.util.List;

import com.tastyhouse.domain.shop.model.ShopRequestStatus;

/**
 * 요청처리 현황 필터 카탈로그 — 요청 유형·상태 전체 목록.
 *
 * <p><b>챕터 09</b>에서 신설. 카탈로그는 DB 조회가 아니라 도메인 enum의 {@code values()}를 훑어
 * 만드는데 {@code values()}는 api 모듈에 허용된 accessor가 아니므로
 * ({@code apiModuleShouldOnlyReadDomainEnums}) 목록 구성이 application에 남아야 한다
 * ({@link ShopChangeCategoryResult}와 같은 이유).
 */
public record ShopRequestTypeCatalogResult(
    List<ShopRequestTypeView> requestTypes,
    List<ShopRequestStatus> statuses
) {
}
