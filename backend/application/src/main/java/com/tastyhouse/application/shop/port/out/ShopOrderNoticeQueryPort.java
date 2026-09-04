package com.tastyhouse.application.shop.port.out;

import java.util.Optional;

/**
 * 주문 시 안내문구 조회 포트(CQRS query 측 아웃바운드 포트) — 회원 화면용.
 *
 * <p>주문 화면에 노출할 안내문구만 조회한다(노출 설정이 켜진 경우). 점주·관리자가 문구 자체를
 * 확인하는 조회는 {@link ShopOrderNoticeManagementQueryPort}가 소유한다.
 */
public interface ShopOrderNoticeQueryPort {

    Optional<ShopOrderNoticeResult> findVisibleOrderNotice(Long shopId);
}
