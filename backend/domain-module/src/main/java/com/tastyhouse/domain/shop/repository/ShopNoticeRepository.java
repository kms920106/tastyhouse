package com.tastyhouse.domain.shop.repository;

import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopNotice;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 점주 공지 write 포트.
 *
 * <p>목록·페이징 조회는 infrastructure-module의 {@code shop/query/ShopNoticeQueryDao}가 담당한다.
 * {@link #findExposedByShopId(ShopId)}는 "가게당 노출 공지는 최대 1건" 불변식 검증에 쓰이므로 write
 * 포트에 남는다({@code ShopNoticeExposureService}가 소비).
 */
public interface ShopNoticeRepository {

    ShopNotice save(ShopNotice shopNotice);

    Optional<ShopNotice> findById(Long id);

    /**
     * 가게에서 현재 앱에 노출 중인 공지. 노출 1건 불변식 검증에 쓰인다.
     */
    Optional<ShopNotice> findExposedByShopId(ShopId shopId);

    void deleteById(Long id);
}
