package com.tastyhouse.core.domain.order.infrastructure.persistence;

import com.tastyhouse.core.domain.order.domain.model.OrderItemOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemOptionJpaRepository extends JpaRepository<OrderItemOption, Long> {
}
