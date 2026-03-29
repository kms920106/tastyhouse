package com.tastyhouse.core.repository.order;

import com.tastyhouse.core.entity.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepository extends JpaRepository<Order, Long> {
}
