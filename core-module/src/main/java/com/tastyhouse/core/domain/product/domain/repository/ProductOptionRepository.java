package com.tastyhouse.core.domain.product.domain.repository;

import java.util.Optional;

import com.tastyhouse.core.domain.product.domain.model.ProductOption;
import com.tastyhouse.core.domain.product.domain.vo.ProductOptionId;

/**
 * 상품 옵션 write 포트. 표현 목적 옵션 목록·배치 조회는 {@code ProductQueryDao}가 담당한다.
 */
public interface ProductOptionRepository {

    Optional<ProductOption> findById(ProductOptionId id);

    ProductOption save(ProductOption productOption);
}
