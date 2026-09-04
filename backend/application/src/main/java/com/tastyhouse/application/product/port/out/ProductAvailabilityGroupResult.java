package com.tastyhouse.application.product.port.out;

import java.util.List;

/**
 * 판매상태 관리 목록의 카테고리 한 묶음 — 카테고리 정보와 그 안의 메뉴들.
 *
 * <p><b>챕터 09</b>에서 신설. DAO는 메뉴 행을 평평하게 돌려주고 <b>카테고리 그룹핑은 유스케이스가
 * 등장 순서를 유지하며</b>({@code LinkedHashMap}) 수행하므로 application의 일이다. 카테고리 미지정
 * 메뉴({@code categoryId == null})도 한 묶음으로 모여 화면에서 "분류 없음"으로 표시된다.
 */
public record ProductAvailabilityGroupResult(
    Long categoryId,
    String categoryName,
    Integer categorySort,
    List<ProductAvailabilityItemResult> products
) {
}
