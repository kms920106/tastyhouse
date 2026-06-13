package com.tastyhouse.core.domain.order.domain.repository;

import com.tastyhouse.core.domain.order.domain.model.OrderProductOption;

import java.util.List;

public interface OrderProductOptionRepository {

    List<OrderProductOption> findByOrderProductId(Long orderProductId);

    void save(OrderProductOption orderProductOption);
}
