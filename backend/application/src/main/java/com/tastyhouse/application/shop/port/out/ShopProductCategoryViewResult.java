package com.tastyhouse.application.shop.port.out;

import java.util.List;

import com.tastyhouse.application.product.port.out.ShopProductItemResult;

/**
 * 가게 상품 카테고리 한 묶음(손님 화면).
 *
 * <p><b>챕터 10</b>에서 신설. 상품을 카테고리로 묶는 그룹핑과, 카테고리가 없는(미분류) 메뉴를 마지막
 * 묶음으로 덧붙이는 규칙(미분류 메뉴가 없으면 묶음 자체를 넣지 않는다)이 모두 계산이라 서비스에
 * 남는다 — 이 record는 그 결과인 "카테고리명 + 그 묶음의 상품 목록"만 담는다. 미분류 묶음의
 * 표시명({@code "미분류"})도 서비스가 정한다.
 *
 * <p>{@code products}는 공유 읽기 계약을 그대로 담는다 — 상품 요약 응답은 표현 조립이 없어 Response의
 * {@code from}이 직접 복사할 수 있다.
 */
public record ShopProductCategoryViewResult(
    String categoryName,
    List<ShopProductItemResult> products
) {
}
