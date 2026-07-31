package com.tastyhouse.domain.shop.domain.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.shop.domain.model.ShopTemporaryClosure;

/**
 * 가게 임시 휴무 write 포트.
 *
 * <p>표현 목적 목록 조회는 infrastructure-module의 {@code infrastructure/shop/query/ShopQueryDao}로
 * 이관했다. {@link #findByShopId(Long)}는 <b>누적 휴무일수 제한 검증</b>과 <b>영업 상태 판정</b>에
 * 쓰이는 도메인 서비스 조회이므로 write 포트에 남는다(공통 지침의 write 포트 잔류 판정 기준).
 */
public interface ShopTemporaryClosureRepository {

    ShopTemporaryClosure save(ShopTemporaryClosure shopTemporaryClosure);

    /**
     * 가게의 임시 휴무 전체. 누적 일수 제한 검증과 영업 상태 판정에 쓰인다.
     */
    List<ShopTemporaryClosure> findByShopId(Long shopId);

    Optional<ShopTemporaryClosure> findById(Long id);

    void deleteById(Long id);
}
