package com.tastyhouse.domain.product.repository;

import java.util.List;

import com.tastyhouse.domain.product.model.ProductCommonOptionGroup;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;

/**
 * 상품 공통 옵션 그룹 write 포트. 표현 목적 조회는 {@code ProductQueryDao}가 담당한다.
 */
public interface ProductCommonOptionGroupRepository {

    ProductCommonOptionGroup save(ProductCommonOptionGroup productCommonOptionGroup);

    /**
     * 대상 공통 옵션들이 속한 옵션그룹을 한 번에 로드한다. 그룹별 {@code minSelect} 제약 판정과
     * 소유 가게 역조회({@code productId} → 상품의 {@code shopId})에 쓴다.
     *
     * <p>공통 옵션그룹은 자체 id VO를 두지 않고 {@link ProductOptionGroupId}를 공유한다
     * (모델·매퍼가 이미 그 타입으로 그룹을 참조한다).
     */
    List<ProductCommonOptionGroup> findAllByIdIn(List<ProductOptionGroupId> ids);
}
