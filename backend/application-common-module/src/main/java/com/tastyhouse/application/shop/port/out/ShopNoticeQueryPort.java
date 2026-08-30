package com.tastyhouse.application.shop.port.out;

import java.util.Optional;

/**
 * 가게 공지 회원 노출 조회 포트(CQRS query 측 아웃바운드 포트).
 *
 * <p>가게 상세 화면에 노출할 공지 한 건만 조회한다. 점주가 자기 가게 공지를 관리하는 조회는
 * {@link ShopNoticeOwnerQueryPort}, 관리자가 검수하는 조회는 {@link ShopNoticeManagementQueryPort}가
 * 소유한다 — 세 계약은 공유 메서드가 0개다.
 */
public interface ShopNoticeQueryPort {

    Optional<ShopNoticeResult> findExposedNotice(Long shopId);
}
