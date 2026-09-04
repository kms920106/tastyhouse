package com.tastyhouse.application.order.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.order.model.OrderStatus;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.payment.model.PaymentStatus;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.order.port.out.OrderDetailResult;
import com.tastyhouse.application.order.port.out.OrderManagementListItemResult;
import com.tastyhouse.application.order.port.out.OrderManagementQueryPort;
import com.tastyhouse.application.order.port.out.OrderSearchCondition;
import com.tastyhouse.application.order.port.in.OrderManagementQueryUseCase;

/**
 * 주문 관리 조회 서비스(admin-api).
 *
 * <p>infra query DAO({@link OrderManagementQueryPort})만 주입해 조회한다. write 포트는 주입하지 않는다.
 *
 * <p>enum 후보값은 HTTP 경계에서 {@code String}으로 받아 여기서 {@code Enum.from(...)}으로 승격한다
 * (도메인 enum 경계 규칙). 관리자 조회는 회원 스코프가 없어 소유권 검증을 하지 않는다.
 *
 * <p><b>챕터 06</b> — 읽기 포트의 {@code *Result}를 그대로 반환하고 Response로 변환하지 않는다.
 * 표현 계약(@Schema 붙은 Response·PaginationResponse) 조립은 컨트롤러의 책임이다.
 */
@Service
@AdminApp
@Transactional(readOnly = true)
public class OrderManagementQueryService implements OrderManagementQueryUseCase {

    private final OrderManagementQueryPort orderManagementQueryPort;

    public OrderManagementQueryService(OrderManagementQueryPort orderManagementQueryPort) {
        this.orderManagementQueryPort = orderManagementQueryPort;
    }

    /**
     * 주문 관리 목록.
     */
    @Override
    public PageResult<OrderManagementListItemResult> getOrders(
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
    ) {
        OrderSearchCondition condition = OrderSearchCondition.of(
            shopId,
            orderStatus == null ? null : OrderStatus.from(orderStatus),
            orderMethod == null ? null : OrderMethod.from(orderMethod),
            paymentStatus == null ? null : PaymentStatus.valueOf(paymentStatus),
            orderNumber,
            ordererName,
            startDate,
            endDate
        );
        PageQuery pageQuery = PageQuery.of(page, size);
        return orderManagementQueryPort.findOrders(condition, pageQuery);
    }

    /**
     * 주문 관리 상세.
     */
    @Override
    public OrderDetailResult getOrder(Long id) {
        return orderManagementQueryPort.findOrderDetail(OrderId.of(id))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND));
    }
}
