package com.tastyhouse.application.shop.port.out;

import java.util.List;

/**
 * 배달권역 조정 요청 점주 조회 포트(CQRS query 측 아웃바운드 포트).
 *
 * <p>점주가 자기 가게에 올린 조정 요청 목록을 조회한다. 관리자가 검수하는 조회는
 * {@code ShopDeliveryAreaAdjustmentManagementQueryPort}가 소유한다 — 공유 메서드는 0개다.
 */
public interface ShopDeliveryAreaAdjustmentQueryPort {

    List<ShopDeliveryAreaAdjustmentListItemResult> findAdjustmentRequests(Long shopId);
}
