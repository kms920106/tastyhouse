package com.tastyhouse.application.order.port.out;

import java.util.Optional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

/**
 * 주문 조회 포트(CQRS query 측 아웃바운드 포트) — 회원 화면용.
 *
 * <p>회원이 자기 주문 목록·상세를 보는 조회와, 리뷰 작성 권한 판정에 쓰는 주문 소유 확인을 담당한다.
 * 관리 화면 조회는 {@code OrderManagementQueryPort}가 소유한다.
 *
 * <p>{@link #findOrderDetail}은 두 포트가 함께 쓰는 <b>공유 메서드</b>라 양쪽에 선언만 중복한다.
 * 반면 분할 전 한 인터페이스 안에서 오버로드였던 {@code findOrders}는 회원용({@code MemberId})과
 * 관리용({@code OrderSearchCondition})의 시그니처가 서로 달라, 분할로 각자의 포트에 하나씩 남는다.
 */
public interface OrderQueryPort {

    PageResult<OrderListItemResult> findOrders(MemberId memberId, PageQuery pageQuery);

    /** 공유 메서드 — {@code OrderManagementQueryPort}에도 같은 시그니처로 선언돼 있다. */
    Optional<OrderDetailResult> findOrderDetail(OrderId orderId);

    Optional<OrderProductOwnershipResult> findOrderProductOwnership(Long orderProductId);

    Optional<Long> findOrderMemberId(Long orderId);
}
