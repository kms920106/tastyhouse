package com.tastyhouse.domain.shop.domain.repository;

import java.util.Optional;

import com.tastyhouse.domain.shop.domain.model.ShopHygieneBadge;

/**
 * 가게 위생 인증 뱃지 write 포트.
 *
 * <p>목록 조회는 infrastructure-module의 {@code infrastructure/shop/query/ShopQueryDao}로 이관했다
 * (공통 지침 패턴 4).
 */
public interface ShopHygieneBadgeRepository {

    Optional<ShopHygieneBadge> findById(Long id);

    ShopHygieneBadge save(ShopHygieneBadge shopHygieneBadge);

    void deleteById(Long id);
}
