package com.tastyhouse.application.shop.port.out;

import java.util.List;

import com.tastyhouse.domain.product.model.StorePriceUnverifiedReason;

/**
 * 매장가격 인증 현황 — 최신 인증 요청의 상태와 인증을 충족하지 못한 메뉴 목록.
 *
 * <p><b>챕터 09</b>에서 신설. 세 출처를 합친다 — 최신 인증 요청(도메인 애그리거트
 * {@code StorePriceVerification}), 인증 여부 플래그, 미충족 메뉴 목록(도메인 서비스
 * {@code StorePriceVerificationService}). 앞의 둘은 애그리거트에서, 마지막은 도메인 서비스에서 나오므로
 * 표현 계약이 직접 받을 수 없다({@code apiModuleShouldBeDomainModelFree}).
 *
 * <p>미충족 사유는 {@code domain.product.service}의 {@code StorePriceUnverifiedItem}을 그대로 넘기지
 * 않고 {@link UnverifiedItem}으로 옮겨 담는다 — 그 타입은 도메인 <b>서비스</b> 패키지에 있어 api 모듈의
 * carve-out(예외·페이징·도메인 enum) 어디에도 들어가지 않는다. 사유 enum 자체는 carve-out 대상이라
 * 그대로 나르고, 문자열 강등은 표현 계약이 수행한다.
 */
public record ShopStorePriceVerificationViewResult(
    Long id,
    String status,
    boolean verified,
    String rejectReason,
    List<UnverifiedItem> unverifiedItems
) {

    /** 인증을 충족하지 못한 메뉴 한 건. */
    public record UnverifiedItem(
        Long productId,
        String productName,
        StorePriceUnverifiedReason reason
    ) {
    }
}
