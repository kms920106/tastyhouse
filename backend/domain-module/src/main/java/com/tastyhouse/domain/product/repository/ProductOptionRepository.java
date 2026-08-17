package com.tastyhouse.domain.product.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.product.vo.ProductOptionId;

/**
 * 상품 옵션 write 포트. 표현 목적 옵션 목록·배치 조회는 {@code ProductQueryDao}가 담당한다.
 */
public interface ProductOptionRepository {

    Optional<ProductOption> findById(ProductOptionId id);

    ProductOption save(ProductOption productOption);

    /**
     * 일괄 품절·숨김 처리 대상 옵션을 한 번에 로드한다.
     *
     * <p>가게 소유권은 이 포트에서 걸러지지 않는다 — 옵션은 {@code option_group_id → product_id →
     * shop_id}로 두 단계 역조회가 필요하므로, 호출부가 옵션그룹의 소유 가게를 별도로 확인해야 한다.
     */
    List<ProductOption> findAllByIdIn(List<ProductOptionId> ids);

    /**
     * 옵션그룹에 속한 옵션 전체. 옵션그룹별 {@code minSelect} 잔여 판매중 개수를 세는 데 쓴다
     * (품절·숨김 부분실패 제약).
     */
    List<ProductOption> findAllByOptionGroupId(ProductOptionGroupId optionGroupId);

    /**
     * 자동해제 시각이 지난 품절 옵션을 조회한다. 품절 자동해제 배치가 대상을 뽑는 데 쓴다.
     */
    List<ProductOption> findAllSoldOutExpiredBefore(LocalDateTime baseTime);
}
