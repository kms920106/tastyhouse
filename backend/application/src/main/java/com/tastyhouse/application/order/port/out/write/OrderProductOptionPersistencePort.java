package com.tastyhouse.application.order.port.out.write;

import com.tastyhouse.domain.order.model.OrderProductOption;

public interface OrderProductOptionPersistencePort {

    void save(OrderProductOption orderProductOption);
}
