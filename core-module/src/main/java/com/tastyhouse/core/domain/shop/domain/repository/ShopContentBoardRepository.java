package com.tastyhouse.core.domain.shop.domain.repository;

import java.util.Optional;

import com.tastyhouse.core.domain.shop.domain.model.ShopContentBoard;

/**
 * 가게 콘텐츠보드 write 포트.
 *
 * <p>목록·페이징 조회는 infrastructure-module의 {@code infrastructure/shop/query/ShopQueryDao}로
 * 이관했다(공통 지침 패턴 4). {@link #countByShopId(Long)}는 등록 개수 제한(최대 4건) 불변식 검증에
 * 쓰이므로 write 포트에 남는다.
 */
public interface ShopContentBoardRepository {

    ShopContentBoard save(ShopContentBoard shopContentBoard);

    Optional<ShopContentBoard> findById(Long id);

    void deleteById(Long id);

    /**
     * 가게의 콘텐츠보드 등록 수. 등록 개수 제한 검증에 쓰인다.
     */
    long countByShopId(Long shopId);
}
