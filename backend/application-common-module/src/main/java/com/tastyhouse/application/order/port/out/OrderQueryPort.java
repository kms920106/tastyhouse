package com.tastyhouse.application.order.port.out;

import java.util.Optional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

/**
 * order 읽기 포트(CQRS query 측 아웃바운드 포트).
 *
 * <p>완전 매핑 전환으로 <b>응용 계층이 읽기 계약을 소유</b>하고 infrastructure-module의
 * {@code OrderQueryDao}가 이를 구현한다. 소비 모듈은 이 인터페이스와 같은 패키지의 반환 DTO
 * ({@code *Result})·검색 조건({@code *SearchCondition})만 알며, QueryDSL도 어댑터의 존재도 알지 않는다.
 *
 * <p>메서드명·시그니처는 DAO의 기존 공개 표면을 그대로 전사한 것이다(챕터 04는 순수 소유권 이동이라
 * 조회 동작·wire 계약을 바꾸지 않는다).
 */
public interface OrderQueryPort {

    PageResult<OrderListItemResult> findOrders(MemberId memberId, PageQuery pageQuery);

    PageResult<OrderManagementListItemResult> findOrders(OrderSearchCondition condition, PageQuery pageQuery);

    Optional<OrderDetailResult> findOrderDetail(OrderId orderId);

    /**
     * 주문 상품 한 건의 소유·상품 식별 정보. 리뷰 작성 화면의 접근 판정용 표현 조회다.
     */
    Optional<OrderProductOwnershipResult> findOrderProductOwnership(Long orderProductId);

    /**
     * 주문의 주문자 회원 ID. 조회 화면의 접근 판정용 표현 조회이므로 애그리거트를 로드하지 않는다.
     */
    Optional<Long> findOrderMemberId(Long orderId);

}
