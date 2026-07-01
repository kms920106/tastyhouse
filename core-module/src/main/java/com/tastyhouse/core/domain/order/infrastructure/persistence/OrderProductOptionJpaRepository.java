package com.tastyhouse.core.domain.order.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tastyhouse.core.domain.order.domain.model.OrderProductOption;

public interface OrderProductOptionJpaRepository extends JpaRepository<OrderProductOption, Long> {
}
