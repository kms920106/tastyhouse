package com.tastyhouse.application.product.port.out;

/**
 * 알레르기 유발성분 카탈로그 항목 — 코드와 한글 라벨.
 *
 * <p><b>챕터 09</b>에서 신설. 카탈로그는 도메인 enum의 {@code values()}를 훑어 만들고
 * ({@code apiModuleShouldOnlyReadDomainEnums}가 api 모듈의 {@code values()} 호출을 금지한다) 법령 열거
 * 순서를 유지해야 하므로 목록 구성이 application에 남는다. 인바운드 포트 반환 타입에 도메인 enum이
 * 실리지 않게 문자열로 강등해 나른다({@code commandRecordsShouldBeBoundaryTyped}).
 */
public record ProductAllergenTypeView(
    String code,
    String label
) {
}
