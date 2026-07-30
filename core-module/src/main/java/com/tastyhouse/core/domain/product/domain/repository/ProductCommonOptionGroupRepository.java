package com.tastyhouse.core.domain.product.domain.repository;

import com.tastyhouse.core.domain.product.domain.model.ProductCommonOptionGroup;

/**
 * 상품 공통 옵션 그룹 write 포트. 표현 목적 조회는 {@code ProductQueryDao}가 담당한다.
 */
public interface ProductCommonOptionGroupRepository {

    ProductCommonOptionGroup save(ProductCommonOptionGroup productCommonOptionGroup);
}
