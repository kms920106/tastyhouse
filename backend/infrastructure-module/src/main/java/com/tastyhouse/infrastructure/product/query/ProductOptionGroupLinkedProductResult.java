package com.tastyhouse.infrastructure.product.query;

/**
 * 옵션그룹을 사용하는 메뉴 read model — 연결 해제 전 영향 확인 화면에 쓴다.
 *
 * @param shopId 이 메뉴의 소유 가게. <b>소유권 판정의 근거</b>다 — 옵션그룹은 자기 가게를 모르므로
 *     호출부가 이 값을 요청의 {@code shopId}와 대조해야 한다.
 */
public record ProductOptionGroupLinkedProductResult(
    Long id,
    Long shopId,
    String name
) {
}
