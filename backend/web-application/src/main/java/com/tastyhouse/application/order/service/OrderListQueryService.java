package com.tastyhouse.application.order.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.payment.model.PaymentStatus;
import com.tastyhouse.application.order.port.in.OrderListQueryUseCase;
import com.tastyhouse.application.order.port.out.OrderListItemResult;
import com.tastyhouse.application.order.port.out.OrderQueryPort;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class OrderListQueryService implements OrderListQueryUseCase {

    private final OrderQueryPort orderQueryPort;

    public OrderListQueryService(OrderQueryPort orderQueryPort) {
        this.orderQueryPort = orderQueryPort;
    }

    @Override
    public PageResult<OrderListItemResult> getOrderList(Long memberId, int page, int size) {
        return orderQueryPort.findOrders(
            MemberId.of(memberId).value(),
            List.of(PaymentStatus.COMPLETED.name(), PaymentStatus.CANCELLED.name()),
            PageQuery.of(page, size)
        );
    }
}
