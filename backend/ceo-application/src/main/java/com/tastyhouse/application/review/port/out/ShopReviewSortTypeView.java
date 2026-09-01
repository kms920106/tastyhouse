package com.tastyhouse.application.review.port.out;

import java.time.LocalDateTime;

/**
 * 리뷰 정렬 설정 — 정렬 방식 코드·한글 표시명·최종 변경 일시.
 *
 * <p><b>챕터 09</b>에서 신설. 표시명은 원래 "화면 소관이니 표현 계약이 붙인다"고 판단해 Response로
 * 내렸으나, <b>도메인 enum에 대한 {@code switch}가 바이트코드에서 {@code ordinal()}·{@code values()}
 * 호출로 컴파일되어</b> {@code apiModuleShouldOnlyReadDomainEnums}(허용 accessor는
 * {@code name}·{@code getDescription}·{@code getDisplayName} 3종)에 걸린다. 즉 api 모듈에서는 도메인
 * enum을 switch할 수 없다.
 *
 * <p>그래서 표시명 매핑을 application에 두고 <b>이미 문자열로 강등된 값</b>을 나른다. {@code switch}를
 * 유지하는 이유는 그대로다 — 상수가 추가되면 컴파일이 깨져 문구 누락이 드러난다.
 */
public record ShopReviewSortTypeView(
    String sortType,
    String sortTypeDescription,
    LocalDateTime updatedAt
) {
}
