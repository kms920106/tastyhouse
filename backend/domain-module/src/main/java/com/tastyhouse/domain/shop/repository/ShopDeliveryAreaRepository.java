package com.tastyhouse.domain.shop.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shop.model.ShopDeliveryArea;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 가게 배달가능지역 write 포트.
 *
 * <p>여기 남는 조회는 전부 불변식 검증(중복 등록 방지·삭제 대상 로드)이나 주문 접수 시의 배달가능 여부
 * 판정에 쓰이므로 write 포트 잔류 기준을 만족한다. 점주 설정 화면의 표현용 목록(행정동 이름 조인)은
 * infrastructure-module의 {@code shop/query/ShopDeliveryAreaQueryDao}가 별도로 담당한다.
 */
public interface ShopDeliveryAreaRepository {

    List<ShopDeliveryArea> findByShopId(ShopId shopId);

    Optional<ShopDeliveryArea> findById(Long deliveryAreaId);

    boolean existsByShopIdAndAdminDongId(ShopId shopId, AdminDongId adminDongId);

    /**
     * 가게의 배달가능지역 등록 건수. 0건이면 "배달가능지역 미설정"이라 주문 접수 시 지역 검사를 생략한다.
     */
    long countByShopId(ShopId shopId);

    ShopDeliveryArea save(ShopDeliveryArea shopDeliveryArea);

    void deleteById(Long deliveryAreaId);
}
