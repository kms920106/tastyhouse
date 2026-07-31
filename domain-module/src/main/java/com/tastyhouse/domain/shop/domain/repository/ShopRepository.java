package com.tastyhouse.domain.shop.domain.repository;

import java.util.Optional;

import com.tastyhouse.domain.shop.domain.model.Shop;
import com.tastyhouse.domain.shop.domain.vo.ShopId;

/**
 * 가게 write 포트.
 *
 * <p>command 경로·도메인 서비스가 트랜잭션 안에서 소비하는 도메인 모델 반환 CRUD만 둔다. 목록·검색·
 * 베스트·즐겨찾기 등 표현 목적 read는 infrastructure-module의
 * {@code infrastructure/shop/query/ShopSearchQueryDao}로 이관했다(공통 지침 패턴 4).
 */
public interface ShopRepository {

    Optional<Shop> findById(ShopId id);

    /**
     * 회원 노출용 단건 조회. 폐업(permanentlyClosed)·노출정지(hidden) 가게는 조회되지 않아,
     * 딥링크로 비노출 가게 상세에 진입하는 것을 차단한다. admin/ceo는 {@link #findById(ShopId)}를 쓴다.
     */
    Optional<Shop> findVisibleById(ShopId id);

    Shop save(Shop shop);
}
