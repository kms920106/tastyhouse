package com.tastyhouse.core.domain.order.infrastructure.persistence;

import com.tastyhouse.core.domain.order.domain.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemJpaRepository extends JpaRepository<OrderItem, Long> {
}
