package com.tastyhouse.application.shop.port.out;

import java.util.List;

/**
 * 가게 포토 카테고리 한 묶음(손님 화면).
 *
 * <p><b>챕터 10</b>에서 신설. 이미지는 카테고리별로 조회되지 않고 <b>전체를 한 번에</b> 읽어
 * {@code shopPhotoCategoryId}로 그룹핑하므로, 그 그룹핑과 URL 추출이 서비스에 남는다. 이 record는
 * 묶음의 결과만 담는다.
 */
public record ShopPhotoCategoryViewResult(
    String name,
    List<String> imageUrls
) {
}
