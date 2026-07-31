package com.tastyhouse.domain.order.domain.repository;

import java.util.Optional;

import com.tastyhouse.domain.order.domain.model.OrderProduct;
import com.tastyhouse.domain.order.domain.vo.OrderProductId;

/**
 * 주문 상품 라인 write 포트.
 *
 * <p>주문 접수 시 라인을 저장하고 가격 갱신 후 다시 저장하는 command 경로, 그리고 리뷰 작성 자격 확인처럼
 * 라인 단건을 불변식 검증에 쓰는 경로만 남긴다. 주문 상세 화면의 라인 목록 조회는
 * {@code infrastructure/order/query/OrderQueryDao}가 담당한다(공통 지침 패턴 4).
 */
public interface OrderProductRepository {

    Optional<OrderProduct> findById(OrderProductId orderProductId);

    OrderProduct save(OrderProduct orderProduct);
}
