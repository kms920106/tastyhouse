package com.tastyhouse.application.order.port.out.write;

import com.tastyhouse.domain.order.model.OrderProductOption;

public interface OrderProductOptionSavePort {

    void save(OrderProductOption orderProductOption);
}
