package com.tastyhouse.application.product.port.out;

import java.util.List;

/**
 * 옵션그룹 하나와 그에 연결된 메뉴 목록.
 *
 * <p><b>챕터 09</b>에서 신설. DAO는 {@code Map<optionGroupId, 메뉴목록>}으로 돌려주는데 <b>맵을 그대로
 * 넘기면 순회 순서를 표현 계층이 정하게 된다.</b> 유스케이스가 순서 있는 {@code List}로 펼쳐 넘긴다.
 */
public record ProductOptionGroupLinkedProductsResult(
    Long optionGroupId,
    List<ProductOptionGroupLinkedProductResult> products
) {
}
