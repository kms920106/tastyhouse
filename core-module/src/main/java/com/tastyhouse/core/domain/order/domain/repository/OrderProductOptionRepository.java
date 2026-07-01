package com.tastyhouse.core.domain.order.domain.repository;

import java.util.List;

import com.tastyhouse.core.domain.order.domain.model.OrderProductOption;

public interface OrderProductOptionRepository {

    List<OrderProductOption> findByOrderProductId(Long orderProductId);

    void save(OrderProductOption orderProductOption);
}
