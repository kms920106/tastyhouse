package com.tastyhouse.application.order.port.out.write;

import com.tastyhouse.domain.order.model.OrderProductOption;

public interface OrderProductOptionRepository {
    void save(OrderProductOption orderProductOption);
}
