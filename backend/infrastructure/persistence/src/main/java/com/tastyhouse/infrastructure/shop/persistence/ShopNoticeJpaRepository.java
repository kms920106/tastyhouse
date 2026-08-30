package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopNoticeJpaRepository extends JpaRepository<ShopNoticeJpaEntity, Long> {

    /**
     * 노출중 공지 1건. <b>{@code findBy~}가 아니라 {@code findFirstBy~}인 것이 중요하다</b> — 노출 1건
     * 불변식은 도메인 서비스가 지키고 DB 제약이 없으므로(MySQL 부분 유니크 인덱스 미지원)
     * {@code is_exposed = 1}이 2건 이상인 상태가 물리적으로 가능한데, 단건 시그니처는 그때
     * {@code IncorrectResultSizeDataAccessException}으로 해당 가게의 공지 기능을 통째로 500으로 만든다.
     * 최신 1건을 결정적으로 고르면 다음 {@code expose} 호출이 나머지를 자연스럽게 정리한다.
     */
    Optional<ShopNoticeJpaEntity> findFirstByShopIdAndExposedIsTrueOrderByIdDesc(Long shopId);
}
