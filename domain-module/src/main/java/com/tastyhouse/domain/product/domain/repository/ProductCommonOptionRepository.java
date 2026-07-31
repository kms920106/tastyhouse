package com.tastyhouse.domain.product.domain.repository;

import com.tastyhouse.domain.product.domain.model.ProductCommonOption;

/**
 * 상품 공통 옵션 write 포트. 표현 목적 조회는 {@code ProductQueryDao}가 담당한다.
 */
public interface ProductCommonOptionRepository {

    ProductCommonOption save(ProductCommonOption productCommonOption);
}
