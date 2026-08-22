package com.tastyhouse.domain.product.port;

/**
 * 가게의 매장 가격 인증 상태를 읽고 쓰는 출력 포트(product → shop 방향).
 *
 * <p><b>왜 포트인가</b>: 메뉴 가격 저장은 product 컨텍스트의 규칙이지만, "매장가·픽업가를 설정할 수
 * 있는가"와 "배달가가 매장가를 넘어 인증을 내려야 하는가"는 <b>가게 단위 상태</b>다. 컨텍스트 경계
 * 규칙({@code ContextBoundaryTest})이 타 컨텍스트의 {@code model}·{@code repository} 직접 import를
 * 금지하므로, product는 이 포트로만 그 상태를 다룬다 — {@code ShopRepository}를 직접 주입하면
 * 신규 위반이 되고 봉인 목록은 늘릴 수 없다.
 *
 * <p>구현은 infrastructure-module의 {@code StorePriceVerificationAdapter}가
 * {@code ShopRepository}에 위임한다.
 */
public interface StorePriceVerificationPort {

    /** 이 가게가 매장 가격 인증을 받았는지 — 매장가·픽업가 설정 가능 여부의 근거다. */
    boolean isStorePriceVerified(Long shopId);

    /** 인증을 켠다(관리자 승인 시). */
    void verifyStorePrice(Long shopId);

    /** 인증을 내린다(배달가 &gt; 매장가가 되어 재인증이 필요할 때). */
    void clearStorePriceVerification(Long shopId);
}
