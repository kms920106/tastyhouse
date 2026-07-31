package com.tastyhouse.adminapi.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.order.domain.model.OrderStatus;
import com.tastyhouse.domain.order.domain.service.OrderTransitionService;
import com.tastyhouse.domain.order.domain.vo.OrderId;

/**
 * 주문 관리 command 서비스(admin-api).
 *
 * <p>관리자의 주문 상태 변경·삭제를 트랜잭션 경계 안에서 도메인 서비스
 * {@link OrderTransitionService}에 위임한다(공통 지침 패턴 2). 상태 전이 규칙과 저장 책임은 도메인
 * 서비스가 갖고, 이 서비스는 {@code Long → OrderId}·{@code String → OrderStatus} 승격과 경계만 담당한다.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class OrderCommandService {

    private final OrderTransitionService orderTransitionService;

    /**
     * 주문 상태를 변경한다.
     */
    public void changeStatus(Long id, String status) {
        OrderId orderId = OrderId.of(id);
        OrderStatus orderStatus = OrderStatus.from(status);
        orderTransitionService.changeStatus(orderId, orderStatus);
    }

    /**
     * 주문을 삭제한다.
     */
    public void deleteOrder(Long id) {
        OrderId orderId = OrderId.of(id);
        orderTransitionService.delete(orderId);
    }
}
