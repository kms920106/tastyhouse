package com.tastyhouse.application.order.port.out;

import java.util.Collection;
import java.util.Optional;

import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface OrderQueryPort {

    PageResult<OrderListItemResult> findOrders(Long memberId, Collection<String> paymentStatuses, PageQuery pageQuery);

    Optional<OrderDetailResult> findOrderDetail(Long orderId);

    Optional<OrderProductOwnershipResult> findOrderProductOwnership(Long orderProductId);

    Optional<Long> findOrderMemberId(Long orderId);
}
