package com.tastyhouse.adminapi.order.application.port.in;

import java.time.LocalDateTime;

import com.tastyhouse.adminapi.order.adapter.in.web.response.OrderDetailResponse;
import com.tastyhouse.adminapi.order.adapter.in.web.response.OrderListItemResponse;
import com.tastyhouse.apicommon.common.PaginationResponse;

/**
 * 주문 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code OrderQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
public interface OrderQueryUseCase {

    PaginationResponse<OrderListItemResponse> getOrders(
        Long shopId,
        String orderStatus,
        String orderMethod,
        String paymentStatus,
        String orderNumber,
        String ordererName,
        LocalDateTime startDate,
        LocalDateTime endDate,
        int page,
        int size
    );

    OrderDetailResponse getOrder(Long id);
}
