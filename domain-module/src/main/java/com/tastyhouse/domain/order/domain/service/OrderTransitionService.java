package com.tastyhouse.domain.order.domain.service;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.order.domain.model.Order;
import com.tastyhouse.domain.order.domain.model.OrderStatus;
import com.tastyhouse.domain.order.domain.repository.OrderRepository;
import com.tastyhouse.domain.order.domain.vo.OrderId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

/**
 * 주문 상태전이(도메인 서비스).
 *
 * <p>주문 상태전이는 결제·포인트 연쇄의 진입점이다 — 결제 승인/취소, 관리자 수동 상태 변경, 주문 삭제가
 * 모두 이 경로로 모인다. 트리거 액터(회원 · 관리자 · 결제 콜백)가 여러 개인데도 "주문을 PK로 로드해
 * 상태를 전이하고 반드시 저장한다"는 규칙은 하나여야 하므로 도메인 계층에 둔다. 상태 전이 자체의
 * 불변식은 {@link Order}가 갖고, 이 서비스는 로드·전이·저장을 원자로 묶는 오케스트레이션(분류 C)이다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code DomainServiceConfig}가 담당한다. 트랜잭션 경계는 이 서비스를 호출하는
 * 소비 모듈의 command 서비스가 선언한다.
 *
 * <p>{@link Order}는 순수 POJO라 더티 체킹이 없으므로 전이 후 명시적으로
 * {@code orderRepository.save(order)}를 호출한다.
 *
 * <p><b>41-payment 인계 메모 — 공개 시그니처</b>:
 * <pre>
 * Order load(OrderId orderId)                                  // PK 로드(없으면 ORDER_NOT_FOUND)
 * Order loadOwnedBy(OrderId orderId, MemberId memberId)        // 로드 + 소유권 검증(ORDER_ACCESS_DENIED)
 * Order loadOwnedBy(OrderId, MemberId, ErrorCode)              // 로드 + 소유권 검증(실패 코드 지정 — 결제용)
 * void changeStatus(OrderId orderId, OrderStatus status)       // 로드 → 전이 → save
 * void changeStatus(Order order, OrderStatus status)           // 이미 로드된 주문의 전이 → save
 * void confirm(Order order)                                    // 결제 승인 확정 전이 → save
 * void cancel(Order order)                                     // 결제 취소 전이 → save
 * void delete(OrderId orderId)                                 // soft delete → save
 * </pre>
 * 결제 도메인은 같은 트랜잭션에서 주문을 함께 바꾸므로, 이미 로드한 {@link Order}를 넘기는
 * {@code changeStatus(Order, ...)}/{@code confirm}/{@code cancel} 오버로드를 쓰면 중복 조회 없이
 * "전이와 저장은 항상 함께"를 보장할 수 있다.
 *
 * <p>전이 가능 여부는 이 서비스가 판정하지 않는다 — {@link OrderStatus#canTransitionTo} 전이 테이블을
 * {@link Order}가 검증하고, 불가 전이는 {@code BusinessException}(상태별 {@code ORDER_ALREADY_*} ·
 * {@code ORDER_INVALID_STATUS_TRANSITION})으로 전파된다. 이 서비스의 공개 시그니처는 그대로이며,
 * 계약만 "허용 전이일 때 전이·저장하고, 불가 전이는 예외"로 좁혀졌다.
 */
public class OrderTransitionService {

    private final OrderRepository orderRepository;

    public OrderTransitionService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * 주문을 PK로 로드한다 — 없으면 {@code ORDER_NOT_FOUND}.
     */
    public Order load(OrderId orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND));
    }

    /**
     * 주문을 로드하고 요청 회원의 소유인지 검증한다 — 위반 시 {@code ORDER_ACCESS_DENIED}.
     */
    public Order loadOwnedBy(OrderId orderId, MemberId memberId) {
        Order order = load(orderId);
        order.validateOwnership(memberId);
        return order;
    }

    /**
     * 주문을 로드하고 요청 회원의 소유인지 검증한다 — 위반 시 호출자가 지정한 에러 코드로 실패시킨다.
     *
     * <p>결제 경로는 같은 소유권 위반을 주문 조회와 다른 에러 코드
     * ({@code PAYMENT_ORDER_ACCESS_DENIED}·{@code PAYMENT_ACCESS_DENIED})로 알려야 하므로, 검증 규칙
     * 자체는 이 서비스에 두고 실패 코드만 호출자가 고르게 한다.
     */
    public Order loadOwnedBy(OrderId orderId, MemberId memberId, ErrorCode accessDeniedCode) {
        Order order = load(orderId);
        if (!order.getMemberId().equals(memberId)) {
            throw new BusinessException(accessDeniedCode);
        }
        return order;
    }

    /**
     * 주문 상태를 전이하고 저장한다 — 전이 테이블을 통과하지 못하면 {@code BusinessException}.
     */
    public void changeStatus(OrderId orderId, OrderStatus status) {
        changeStatus(load(orderId), status);
    }

    /**
     * 이미 로드된 주문의 상태를 전이하고 저장한다(같은 트랜잭션에서 주문을 함께 다루는 결제 경로용) —
     * 전이 테이블을 통과하지 못하면 {@code BusinessException}.
     */
    public void changeStatus(Order order, OrderStatus status) {
        order.changeStatus(status);
        orderRepository.save(order);
    }

    /**
     * 결제 승인에 따라 주문을 확정하고 저장한다({@code PENDING -> CONFIRMED}).
     */
    public void confirm(Order order) {
        order.confirm();
        orderRepository.save(order);
    }

    /**
     * 결제 취소에 따라 주문을 취소하고 저장한다({@code PENDING|CONFIRMED -> CANCELLED}).
     */
    public void cancel(Order order) {
        order.cancel();
        orderRepository.save(order);
    }

    /**
     * 주문을 삭제(soft delete)하고 저장한다.
     */
    public void delete(OrderId orderId) {
        Order order = load(orderId);
        order.delete();
        orderRepository.save(order);
    }
}
