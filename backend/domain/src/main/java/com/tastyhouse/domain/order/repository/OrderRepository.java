package com.tastyhouse.domain.order.repository;

import java.util.Optional;

import com.tastyhouse.domain.order.model.Order;
import com.tastyhouse.domain.order.vo.OrderId;

/**
 * 주문 write 포트.
 *
 * <p>command 경로·도메인 서비스의 트랜잭션 안에서 소비되는 단건 로드와 저장만 남긴다. 목록·검색·페이징
 * 등 표현 목적 조회는 infrastructure-module의 {@code infrastructure/order/query/OrderQueryDao}가
 * 담당한다(공통 지침 패턴 4).
 */
public interface OrderRepository {

    Optional<Order> findById(OrderId orderId);

    Order save(Order order);
}
