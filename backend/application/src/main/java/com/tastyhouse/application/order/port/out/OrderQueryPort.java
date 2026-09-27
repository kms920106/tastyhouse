package com.tastyhouse.application.order.port.out;

import java.util.Optional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface OrderQueryPort {

    PageResult<OrderListItemResult> findOrders(MemberId memberId, PageQuery pageQuery);

    Optional<OrderDetailResult> findOrderDetail(OrderId orderId);

    Optional<OrderProductOwnershipResult> findOrderProductOwnership(Long orderProductId);

    Optional<Long> findOrderMemberId(Long orderId);
}
