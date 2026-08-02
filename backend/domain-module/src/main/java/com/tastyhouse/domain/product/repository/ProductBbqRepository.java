package com.tastyhouse.domain.product.repository;

import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductBbq;
import com.tastyhouse.domain.product.vo.ProductId;

/**
 * 상품 ↔ BBQ 메뉴 매핑 write 포트.
 *
 * <p>{@link #findByProductId}는 옵션 동기화 완료 표시(상태 전이) 전에 대상을 로드하는 용도라 남는다.
 * 동기화 대상 탐색(표현/배치 목적 조회)은 {@code ProductQueryDao#findFirstBbqSyncTarget}가 담당한다.
 */
public interface ProductBbqRepository {

    Optional<ProductBbq> findByProductId(ProductId productId);

    ProductBbq save(ProductBbq productBbq);
}
