package com.tastyhouse.domain.order.repository;

import com.tastyhouse.domain.order.model.OrderProductOption;

/**
 * 주문 상품 라인 옵션 write 포트.
 *
 * <p>주문 접수 시 선택 옵션을 저장하는 command 경로만 남긴다(라인당 옵션은 접수 시점에만 만들어지고 이후
 * 변경되지 않는다). 주문 상세 화면의 옵션 목록 조회는
 * {@code infrastructure/order/query/OrderQueryDao}가 담당한다(공통 지침 패턴 4).
 */
public interface OrderProductOptionRepository {

    void save(OrderProductOption orderProductOption);
}
