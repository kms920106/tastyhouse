package com.tastyhouse.domain.product.repository;

import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;

/**
 * 상품 옵션 그룹 write 포트. 표현 목적 옵션 목록 조회는 {@code ProductQueryDao#findProductOptions}가 담당한다.
 */
public interface ProductOptionGroupRepository {

    Optional<ProductOptionGroup> findById(ProductOptionGroupId id);

    ProductOptionGroup save(ProductOptionGroup productOptionGroup);
}
