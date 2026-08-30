package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopOrderNoticeJpaRepository extends JpaRepository<ShopOrderNoticeJpaEntity, Long> {

    /**
     * 가게의 주문안내 1건. {@code shop_id}에 유니크 제약이 있어 단건 시그니처가 안전하다 —
     * {@code ShopNotice}의 노출 공지 조회가 {@code findFirstBy~}여야 했던 것(부분 유니크 인덱스
     * 부재로 2건 이상이 물리적으로 가능)과 달리, 여기서는 DB가 1건을 보장하므로
     * {@code IncorrectResultSizeDataAccessException} 위험이 없다.
     */
    Optional<ShopOrderNoticeJpaEntity> findByShopId(Long shopId);
}
