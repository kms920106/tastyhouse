package com.tastyhouse.application.shop.port.out;

/**
 * 가게 사진 카테고리 한 건(회원 상세·관리 화면 공용).
 *
 * <p>두 소비자 모두 카테고리 식별자와 이름만 쓰므로 하나의 Result로 둔다(회원 화면은 이 식별자로
 * {@link ShopPhotoCategoryImageResult} 목록을 묶는다).
 */
public record ShopPhotoCategoryResult(
    Long id,
    String name
) {
}
