package com.tastyhouse.domain.product.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductCommonOption;
import com.tastyhouse.domain.product.vo.ProductCommonOptionId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;

/**
 * 상품 공통 옵션 write 포트. 표현 목적 조회는 {@code ProductQueryDao}가 담당한다.
 *
 * <p>과거에는 {@code save} 하나뿐이라 조회 경로가 전무했다 — 점주 품절·숨김 관리가 공통 옵션도
 * 대상으로 삼게 되면서 단건 로드와 부분실패 검증용 조회가 필요해졌다.
 */
public interface ProductCommonOptionRepository {

    Optional<ProductCommonOption> findById(ProductCommonOptionId id);

    ProductCommonOption save(ProductCommonOption productCommonOption);

    /**
     * 일괄 품절·숨김 처리 대상 공통 옵션을 한 번에 로드한다.
     *
     * <p>가게 소유권은 이 포트에서 걸러지지 않는다 — 호출부가 옵션그룹의 소유 가게를 별도로 확인해야 한다.
     */
    List<ProductCommonOption> findAllByIdIn(List<ProductCommonOptionId> ids);

    /**
     * 공통 옵션그룹에 속한 옵션 전체. 옵션그룹별 {@code minSelect} 잔여 판매중 개수를 세는 데 쓴다.
     *
     * <p>일반 옵션과 공통 옵션은 별도 테이블이므로 각 갈래가 자기 옵션만 센다.
     */
    List<ProductCommonOption> findAllByOptionGroupId(ProductOptionGroupId optionGroupId);

    /**
     * 자동해제 시각이 지난 품절 공통 옵션을 조회한다. 품절 자동해제 배치가 대상을 뽑는 데 쓴다.
     */
    List<ProductCommonOption> findAllSoldOutExpiredBefore(LocalDateTime baseTime);
}
