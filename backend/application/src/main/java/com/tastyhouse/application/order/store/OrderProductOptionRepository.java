package com.tastyhouse.application.order.store;

import com.tastyhouse.domain.order.model.OrderProductOption;

public interface OrderProductOptionRepository {
    void save(OrderProductOption orderProductOption);
}
