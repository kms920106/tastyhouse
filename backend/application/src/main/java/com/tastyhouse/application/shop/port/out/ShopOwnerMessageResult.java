package com.tastyhouse.application.shop.port.out;

import java.time.LocalDateTime;

/**
 * 가게의 최신 사장님 한마디(회원 가게정보·점주 가게소개 화면 공용).
 *
 * <p>회원 화면은 문구와 작성 시각을, 점주 화면은 문구만 쓴다 — 필드 합이 둘뿐이라 하나의 Result로 둔다.
 */
public record ShopOwnerMessageResult(
    String message,
    LocalDateTime createdAt
) {
}
