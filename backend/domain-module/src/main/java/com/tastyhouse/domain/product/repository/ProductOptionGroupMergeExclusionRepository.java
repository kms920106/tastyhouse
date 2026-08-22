package com.tastyhouse.domain.product.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductOptionGroupMergeExclusion;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 옵션그룹 합치기 추천 제외 write 포트(append-only).
 *
 * <p>{@code findAllByShopId}는 표현이 아니라 <b>추천 목록 필터링</b>에 쓰인다 — 서명 계산이 Java에만
 * 있으므로(SQL과 두 벌로 유지하면 인코딩 차이로 제외 기능이 조용히 깨진다) 제외 집합을 여기서 읽어
 * 도메인 서비스가 걸러낸다.
 */
public interface ProductOptionGroupMergeExclusionRepository {

    ProductOptionGroupMergeExclusion save(ProductOptionGroupMergeExclusion exclusion);

    /** 재클릭 멱등을 위한 조회 — {@code UNIQUE (shop_id, group_signature)}와 짝을 이룬다. */
    Optional<ProductOptionGroupMergeExclusion> findByShopIdAndGroupSignature(
        ShopId shopId,
        String groupSignature
    );

    List<ProductOptionGroupMergeExclusion> findAllByShopId(ShopId shopId);
}
