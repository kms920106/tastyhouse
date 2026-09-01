package com.tastyhouse.application.shop.port.out;

import java.util.List;

import com.tastyhouse.domain.shop.model.ShopChangeCategory;
import com.tastyhouse.domain.shop.model.ShopChangeType;

/**
 * 변경이력 대분류 카탈로그 항목 — 대분류 하나와 그에 속한 중분류 목록.
 *
 * <p><b>챕터 09</b>에서 신설. 이 카탈로그는 DB 조회가 아니라 도메인 enum의 {@code values()}를 훑어
 * 만드는데, {@code values()}는 api 모듈에 허용된 읽기 accessor 3종
 * ({@code name}·{@code getDescription}·{@code getDisplayName})에 들어가지 않으므로
 * ({@code apiModuleShouldOnlyReadDomainEnums}) 목록 구성은 application에 남아야 한다. 표현 계약은
 * 이 record를 받아 enum → 문자열 강등만 수행한다.
 */
public record ShopChangeCategoryResult(
    ShopChangeCategory category,
    List<ShopChangeType> changeTypes
) {
}
