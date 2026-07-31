package com.tastyhouse.domain.shop.domain.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.shop.domain.model.ShopSuspension;

/**
 * 가게 영업 임시중지 write 포트.
 *
 * <p>표현 목적 목록 조회는 infrastructure-module의 {@code infrastructure/shop/query/ShopQueryDao}로
 * 이관했다. {@link #findByShopId(Long)}는 <b>영업 상태 판정</b>(활성 임시중지 여부)에 쓰이는 도메인
 * 서비스 조회이므로 write 포트에 남는다(공통 지침의 write 포트 잔류 판정 기준).
 */
public interface ShopSuspensionRepository {

    ShopSuspension save(ShopSuspension shopSuspension);

    /**
     * 가게의 임시중지 전체. 영업 상태 판정(도메인 서비스)에 쓰인다.
     */
    List<ShopSuspension> findByShopId(Long shopId);

    Optional<ShopSuspension> findById(Long id);
}
