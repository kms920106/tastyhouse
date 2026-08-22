package com.tastyhouse.domain.product.repository;

import java.util.List;

import com.tastyhouse.domain.product.model.ProductOptionGroupMergeHistory;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 옵션그룹 합치기 이력 write 포트(append-only).
 *
 * <p>{@code findAllByMergedOptionGroupId}가 <b>"이 그룹은 어디로 갔나"</b> 역조회의 유일한 경로다 —
 * 합치기 후에는 링크가 기준 그룹으로 옮겨져 소유 가게 역조회가 불가능하므로, 이 이력이 없으면
 * 흡수된 그룹에 대한 문의에 답할 근거가 0이다. 감사 목적이라 write 포트에 남긴다.
 */
public interface ProductOptionGroupMergeHistoryRepository {

    ProductOptionGroupMergeHistory save(ProductOptionGroupMergeHistory history);

    /** "이 그룹은 어디로 흡수됐나" 역조회. 그룹당 1행이라 보통 0건 또는 1건이다. */
    List<ProductOptionGroupMergeHistory> findAllByMergedOptionGroupId(ProductOptionGroupId mergedOptionGroupId);

    /** 가게의 합치기 이력 전체(최신순). */
    List<ProductOptionGroupMergeHistory> findAllByShopId(ShopId shopId);
}
