package com.tastyhouse.application.order.port.out;

import java.util.Optional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

public interface OrderQueryPort {

    PageResult<OrderListItemResult> findOrders(MemberId memberId, PageQuery pageQuery);

    Optional<OrderDetailResult> findOrderDetail(OrderId orderId);

    Optional<OrderProductOwnershipResult> findOrderProductOwnership(Long orderProductId);

    Optional<Long> findOrderMemberId(Long orderId);
}
