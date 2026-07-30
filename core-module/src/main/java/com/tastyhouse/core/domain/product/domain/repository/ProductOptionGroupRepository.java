package com.tastyhouse.core.domain.product.domain.repository;

import java.util.Optional;

import com.tastyhouse.core.domain.product.domain.model.ProductOptionGroup;
import com.tastyhouse.core.domain.product.domain.vo.ProductOptionGroupId;

/**
 * 상품 옵션 그룹 write 포트. 표현 목적 옵션 목록 조회는 {@code ProductQueryDao#findProductOptions}가 담당한다.
 */
public interface ProductOptionGroupRepository {

    Optional<ProductOptionGroup> findById(ProductOptionGroupId id);

    ProductOptionGroup save(ProductOptionGroup productOptionGroup);
}
