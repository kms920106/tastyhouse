package com.tastyhouse.core.domain.order.infrastructure.persistence;

import com.tastyhouse.core.domain.order.domain.model.OrderProductOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderProductOptionJpaRepository extends JpaRepository<OrderProductOption, Long> {
}
