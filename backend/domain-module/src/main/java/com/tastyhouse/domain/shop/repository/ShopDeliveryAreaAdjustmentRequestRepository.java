package com.tastyhouse.domain.shop.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.shop.model.DeliveryAreaAdjustmentStatus;
import com.tastyhouse.domain.shop.model.ShopDeliveryAreaAdjustmentRequest;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 배달지역 조정 신청 write 포트.
 *
 * <p>목록·페이징 조회는 이 포트에 두지 않는다 — 표현 목적 조회이므로 infrastructure-module의
 * {@code shop/query/ShopDeliveryAreaAdjustmentQueryDao} 몫이다. {@code existsByShopIdAndStatusIn}은
 * 진행 중 신청 중복 접수를 막는 <b>불변식 검증</b>이라 write 포트에 남는다.
 */
public interface ShopDeliveryAreaAdjustmentRequestRepository {

    Optional<ShopDeliveryAreaAdjustmentRequest> findById(Long id);

    /**
     * 가게에 해당 상태들 중 하나인 신청이 있는지. 진행 중(PENDING·IN_PROGRESS) 중복 신청 차단에 쓰인다.
     */
    boolean existsByShopIdAndStatusIn(ShopId shopId, List<DeliveryAreaAdjustmentStatus> statuses);

    ShopDeliveryAreaAdjustmentRequest save(ShopDeliveryAreaAdjustmentRequest request);
}
