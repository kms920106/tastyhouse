package com.tastyhouse.application.order.port.out;

import java.util.Optional;

import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

/**
 * 주문 관리 화면 조회 포트(CQRS query 측 아웃바운드 포트).
 *
 * <p>전체 주문을 검색 조건으로 조회하는 관리 목록과 상세를 담당한다. 회원 화면 조회는
 * {@code OrderQueryPort}가 소유한다.
 *
 * <p>{@link #findOrderDetail}은 두 포트가 함께 쓰는 <b>공유 메서드</b>라 양쪽에 선언만 중복한다.
 */
public interface OrderManagementQueryPort {

    PageResult<OrderManagementListItemResult> findOrders(OrderSearchCondition condition, PageQuery pageQuery);

    /** 공유 메서드 — {@code OrderQueryPort}에도 같은 시그니처로 선언돼 있다. */
    Optional<OrderDetailResult> findOrderDetail(OrderId orderId);
}
