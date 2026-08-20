package com.tastyhouse.domain.product.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductOptionGroupLink;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;

/**
 * 메뉴 ↔ 일반 옵션그룹 연결 write 포트.
 *
 * <p>{@code findAllByOptionGroupId}가 <b>소유권 판정의 근거</b>다 — 옵션그룹은 자기 가게를 모르므로
 * "그룹 → 링크 → 메뉴 → 가게" 3단으로 역조회해야 한다. 이 조회가 없으면 남의 가게 옵션을
 * 품절 처리하는 것을 막을 수 없어 write 포트에 남긴다.
 */
public interface ProductOptionGroupLinkRepository {

    ProductOptionGroupLink save(ProductOptionGroupLink link);

    Optional<ProductOptionGroupLink> findByProductIdAndOptionGroupId(
        ProductId productId,
        ProductOptionGroupId optionGroupId
    );

    /** 이 메뉴에 연결된 그룹들을 {@code sort} 오름차순으로 반환한다. */
    List<ProductOptionGroupLink> findAllByProductId(ProductId productId);

    /** 이 그룹이 연결된 메뉴들. 소유권 역조회와 "마지막 연결" 판정에 쓴다. */
    List<ProductOptionGroupLink> findAllByOptionGroupId(ProductOptionGroupId optionGroupId);

    /** 여러 그룹의 연결을 한 번에 로드한다(N+1 방지 — 일괄 품절·숨김 소유권 판정). */
    List<ProductOptionGroupLink> findAllByOptionGroupIdIn(List<ProductOptionGroupId> optionGroupIds);

    boolean existsByProductIdAndOptionGroupId(ProductId productId, ProductOptionGroupId optionGroupId);

    void delete(ProductOptionGroupLink link);
}
