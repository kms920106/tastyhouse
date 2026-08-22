package com.tastyhouse.domain.product.service;

import com.tastyhouse.domain.product.model.StorePriceUnverifiedReason;

/**
 * 매장가격 인증을 충족하지 못한 메뉴 한 건 — 점주 화면의 "인증 OFF 사유" 표시용이다.
 *
 * <p>메뉴명을 함께 나르는 이유는 점주가 어느 메뉴를 고쳐야 하는지 즉시 알아야 하기 때문이다.
 * 화면이 메뉴 id로 이름을 다시 조회하게 하면 목록 길이만큼 추가 요청이 발생한다.
 */
public record StorePriceUnverifiedItem(
    Long productId,
    String productName,
    StorePriceUnverifiedReason reason
) {
}
