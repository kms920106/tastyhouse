package com.tastyhouse.domain.shop.domain.repository;

import java.util.Optional;

import com.tastyhouse.domain.shop.domain.model.ShopImageChangeRequest;
import com.tastyhouse.domain.shop.domain.model.ShopImageType;
import com.tastyhouse.domain.shared.model.ApprovalStatus;

/**
 * 가게 이미지 변경요청 write 포트.
 *
 * <p>목록·페이징 조회는 infrastructure-module의 {@code infrastructure/shop/query/ShopQueryDao}로
 * 이관했다(공통 지침 패턴 4). 두 {@code existsBy...}는 PENDING 중복 차단과 노출정지 변경 차단
 * 불변식 검증에 쓰이므로 write 포트에 남는다.
 */
public interface ShopImageChangeRequestRepository {

    ShopImageChangeRequest save(ShopImageChangeRequest shopImageChangeRequest);

    Optional<ShopImageChangeRequest> findById(Long id);

    /**
     * 같은 가게·같은 이미지 유형에 해당 상태의 요청이 있는지. PENDING 중복 요청 차단에 쓰인다.
     */
    boolean existsByShopIdAndImageTypeAndStatus(Long shopId, ShopImageType imageType, ApprovalStatus status);

    /**
     * 가게에 해당 상태의 요청이 있는지. 진행 중 요청이 있을 때 노출정지 변경 차단에 쓰인다.
     */
    boolean existsByShopIdAndStatus(Long shopId, ApprovalStatus status);
}
