package com.tastyhouse.domain.shop.service;

import com.tastyhouse.domain.region.repository.AdminDongRepository;
import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shop.model.ShopDeliveryArea;
import com.tastyhouse.domain.shop.repository.ShopDeliveryAreaRepository;
import com.tastyhouse.domain.shop.repository.ShopDeliveryTipRegionLookup;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

/**
 * 가게 배달가능지역 등록·삭제(도메인 서비스).
 *
 * <p>{@code ShopDeliveryArea}는 행 하나만으로는 판정할 수 없는 규칙 두 가지를 갖는다 —
 * (1) 등록 대상 행정동이 마스터에 실재해야 하고(다른 애그리거트 타입인 {@code AdminDong}의 존재 확인),
 * (2) 같은 가게에 같은 행정동을 두 번 등록할 수 없다(집합 차원). 삭제도 마찬가지로 지역별 배달팁이
 * 그 행정동을 참조하고 있으면 막아야 한다. 이 규칙들이 호출 경로마다 갈리지 않도록 도메인 계층에 둔다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며, 빈 등록은 infrastructure-module의
 * {@code DomainServiceConfig}가 담당한다. 점주 소유권 검증은 ceo-api 계층
 * ({@code ShopOwnershipValidator})의 책임이라 여기서는 다루지 않는다.
 */
public class ShopDeliveryAreaService {

    private final ShopDeliveryAreaRepository shopDeliveryAreaRepository;
    private final AdminDongRepository adminDongRepository;
    private final ShopDeliveryTipRegionLookup shopDeliveryTipRegionLookup;

    public ShopDeliveryAreaService(
        ShopDeliveryAreaRepository shopDeliveryAreaRepository,
        AdminDongRepository adminDongRepository,
        ShopDeliveryTipRegionLookup shopDeliveryTipRegionLookup
    ) {
        this.shopDeliveryAreaRepository = shopDeliveryAreaRepository;
        this.adminDongRepository = adminDongRepository;
        this.shopDeliveryTipRegionLookup = shopDeliveryTipRegionLookup;
    }

    /**
     * 가게에 배달가능지역(행정동)을 추가한다.
     *
     * @return 생성된 배달가능지역의 식별자
     */
    public Long addArea(ShopId shopId, AdminDongId adminDongId) {
        if (!adminDongRepository.existsById(adminDongId)) {
            throw new ResourceNotFoundException(ErrorCode.ADMIN_DONG_NOT_FOUND);
        }

        if (shopDeliveryAreaRepository.existsByShopIdAndAdminDongId(shopId, adminDongId)) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_AREA_DUPLICATED);
        }

        ShopDeliveryArea saved = shopDeliveryAreaRepository.save(ShopDeliveryArea.of(shopId, adminDongId));
        return saved.getId();
    }

    /**
     * 배달가능지역을 삭제한다.
     *
     * <p>그 행정동을 대상으로 하는 지역별 배달팁이 남아 있으면 {@code SHOP_DELIVERY_AREA_IN_USE}(409)로
     * 차단한다 — 지역별 팁이 배달불가 지역을 가리키는 상태를 만들지 않기 위해서다. 점주는 지역별 배달팁을
     * 먼저 정리한 뒤 배달가능지역을 지워야 한다.
     */
    public void removeArea(Long deliveryAreaId) {
        ShopDeliveryArea deliveryArea = shopDeliveryAreaRepository.findById(deliveryAreaId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_DELIVERY_AREA_NOT_FOUND));

        boolean referencedByRegionTip = shopDeliveryTipRegionLookup.existsRegionTipByShopIdAndAdminDongId(
            deliveryArea.getShopId(),
            deliveryArea.getAdminDongId()
        );
        if (referencedByRegionTip) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_AREA_IN_USE);
        }

        shopDeliveryAreaRepository.deleteById(deliveryAreaId);
    }
}
