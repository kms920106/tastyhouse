package com.tastyhouse.ceoapi.shop;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shop.model.ShopDeliveryArea;
import com.tastyhouse.domain.shop.repository.ShopDeliveryAreaRepository;
import com.tastyhouse.domain.shop.service.ShopDeliveryAreaService;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 점주용 가게 배달가능지역 변경 서비스(CQRS command 측).
 *
 * <p>행정동 존재·중복 등록·지역별 배달팁 참조 여부 같은 불변식은 전부 도메인 서비스
 * {@link ShopDeliveryAreaService}가 담당한다. 이 서비스는 소유권 검증과 트랜잭션 경계,
 * 식별자 VO 승격만 책임진다({@code ShopMinOrderAmountCommandService}와 동일 구조).
 *
 * <p><b>삭제도 소유권을 검증한다.</b> 경로에 shopId가 없는 하위 리소스 삭제는 기존에
 * {@code ShopClosedDayCommandService#deleteClosedDay}처럼 검증을 생략하는 관례가 있었지만, 그 관례의
 * 전제는 "대상 행에서 shopId를 역조회할 수단이 없다"는 것이다. 배달가능지역은
 * {@link ShopDeliveryAreaRepository#findById}로 행을 읽어 {@link ShopDeliveryArea#getShopId()}를 얻을 수
 * 있으므로 그 전제가 성립하지 않는다. 검증을 생략하면 아무 점주나 순번을 훑어 <b>남의 가게 배달가능지역을
 * 삭제</b>할 수 있고, 그 결과 피해 가게는 배달 범위를 잃거나(부분 삭제 시 정상 주문이 거절됨) 등록 건수가
 * 0이 되어 주문 접수의 지역 검사 자체가 비활성화된다.
 */
@Service
@Transactional
public class ShopDeliveryAreaCommandService {

    private final ShopDeliveryAreaService shopDeliveryAreaService;
    private final ShopDeliveryAreaRepository shopDeliveryAreaRepository;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopDeliveryAreaCommandService(ShopDeliveryAreaService shopDeliveryAreaService, ShopDeliveryAreaRepository shopDeliveryAreaRepository, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopDeliveryAreaService = shopDeliveryAreaService;
        this.shopDeliveryAreaRepository = shopDeliveryAreaRepository;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    public Long addDeliveryArea(Long ceoId, Long shopId, Long adminDongId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopId targetShopId = ShopId.of(shopId);
        AdminDongId targetAdminDongId = AdminDongId.of(adminDongId);
        return shopDeliveryAreaService.addArea(targetShopId, targetAdminDongId);
    }

    /**
     * 배달가능지역을 삭제한다 — 삭제 대상 행에서 shopId를 역조회해 소유권을 먼저 검증한다.
     */
    public void removeDeliveryArea(Long ceoId, Long deliveryAreaId) {
        ShopDeliveryArea deliveryArea = shopDeliveryAreaRepository.findById(deliveryAreaId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_DELIVERY_AREA_NOT_FOUND));
        shopOwnershipValidator.validateOwnership(ceoId, deliveryArea.getShopId().value());

        shopDeliveryAreaService.removeArea(deliveryAreaId);
    }
}
