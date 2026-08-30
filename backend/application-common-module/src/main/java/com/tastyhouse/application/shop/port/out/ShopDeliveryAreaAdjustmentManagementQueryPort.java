package com.tastyhouse.application.shop.port.out;

import java.util.Optional;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.domain.shop.model.DeliveryAreaAdjustmentStatus;

/**
 * 배달권역 조정 요청 관리 화면 조회 포트(CQRS query 측 아웃바운드 포트).
 *
 * <p>전체 가게의 조정 요청을 처리 상태로 검색하는 관리 목록과 검수용 상세를 조회한다. 점주 조회는
 * {@code ShopDeliveryAreaAdjustmentQueryPort}가 소유한다.
 */
public interface ShopDeliveryAreaAdjustmentManagementQueryPort {

    PageResult<ShopDeliveryAreaAdjustmentListItemResult> findAdjustmentRequestPage(DeliveryAreaAdjustmentStatus status, Long shopId, PageQuery pageQuery);

    Optional<ShopDeliveryAreaAdjustmentDetailResult> findAdjustmentRequestById(Long requestId);
}
