package com.tastyhouse.core.domain.order.infrastructure.persistence;

import com.tastyhouse.core.domain.order.domain.model.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderProductJpaRepository extends JpaRepository<OrderProduct, Long> {
}
