package com.tastyhouse.domain.product.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;

/**
 * 상품 옵션 그룹 write 포트. 표현 목적 옵션 목록 조회는 {@code ProductQueryDao#findProductOptions}가 담당한다.
 */
public interface ProductOptionGroupRepository {

    Optional<ProductOptionGroup> findById(ProductOptionGroupId id);

    ProductOptionGroup save(ProductOptionGroup productOptionGroup);

    /**
     * 대상 옵션들이 속한 옵션그룹을 한 번에 로드한다. 그룹별 {@code minSelect} 제약 판정과
     * 소유 가게 역조회({@code productId} → 상품의 {@code shopId})에 쓴다.
     */
    List<ProductOptionGroup> findAllByIdIn(List<ProductOptionGroupId> ids);
}
