package com.tastyhouse.application.review.port.out;

/**
 * 게시중단 요청 사유 카탈로그 항목 — 코드와 한글 사유명.
 *
 * <p><b>챕터 09</b>에서 신설. 카탈로그는 도메인 enum의 {@code values()}를 훑어 만드는데 그 메서드는
 * api 모듈에 허용된 accessor가 아니므로({@code apiModuleShouldOnlyReadDomainEnums}) 목록 구성이
 * application에 남는다.
 *
 * <p><b>도메인 enum을 그대로 담지 않고 문자열로 강등해 나른다</b> — 인바운드 포트의 반환 타입에
 * {@code com.tastyhouse.domain..}이 실리면 {@code commandRecordsShouldBeBoundaryTyped}(경계 타입 규칙,
 * carve-out은 예외·페이징 계약뿐)에 걸린다. 목록 요소는 제네릭 타입 인자로도 잡힌다.
 */
public record ReviewBlindReasonView(
    String code,
    String description
) {
}
