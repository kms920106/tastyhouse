package com.tastyhouse.domain.order.repository;

import com.tastyhouse.domain.order.model.OrderProductOption;

public interface OrderProductOptionRepository {
    void save(OrderProductOption orderProductOption);
}
