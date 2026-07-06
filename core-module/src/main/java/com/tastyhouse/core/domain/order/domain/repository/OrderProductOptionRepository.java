package com.tastyhouse.core.domain.order.domain.repository;

import java.util.List;

import com.tastyhouse.core.domain.order.domain.model.OrderProductOption;
import com.tastyhouse.core.domain.order.domain.vo.OrderProductId;

public interface OrderProductOptionRepository {

    List<OrderProductOption> findByOrderProductId(OrderProductId orderProductId);

    void save(OrderProductOption orderProductOption);
}
