package com.tastyhouse.domain.shop.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopMenuCollectionImage;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shop.vo.ShopMenuCollectionImageId;

/**
 * 메뉴모음컷 write 포트.
 *
 * <p>{@code findAllByShopId}가 상태 필터 없이 전량을 반환하는 이유는 개수 제한(최대 6개)·최소 1개 유지
 * 같은 <b>집합 차원 불변식</b>이 승인 상태와 무관하게 적용되기 때문이다 — 대기 중인 것도 등록 슬롯을
 * 차지하고, 순서 변경 대상에 포함된다. 상태별로 좁혀 읽으면 그 판정이 조용히 틀린다.
 */
public interface ShopMenuCollectionImageRepository {

    ShopMenuCollectionImage save(ShopMenuCollectionImage image);

    Optional<ShopMenuCollectionImage> findById(ShopMenuCollectionImageId id);

    /** 가게의 메뉴모음컷 전량(상태 무관) — {@code sort} 오름차순. */
    List<ShopMenuCollectionImage> findAllByShopId(ShopId shopId);

    void delete(ShopMenuCollectionImage image);
}
