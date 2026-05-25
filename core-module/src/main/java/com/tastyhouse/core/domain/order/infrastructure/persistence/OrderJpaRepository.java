package com.tastyhouse.core.domain.order.infrastructure.persistence;

import com.tastyhouse.core.domain.order.domain.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepository extends JpaRepository<Order, Long> {
}
