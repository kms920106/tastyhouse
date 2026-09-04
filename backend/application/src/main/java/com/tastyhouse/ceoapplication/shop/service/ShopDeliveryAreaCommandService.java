package com.tastyhouse.ceoapplication.shop.service;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.region.repository.AdminDongRepository;
import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopDeliveryArea;
import com.tastyhouse.domain.shop.repository.ShopDeliveryAreaRepository;
import com.tastyhouse.domain.shop.service.ShopDeliveryAreaPolygonService;
import com.tastyhouse.domain.shop.service.ShopDeliveryAreaRadiusService;
import com.tastyhouse.domain.shop.service.ShopDeliveryAreaService;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.geo.GeoPoint;
import com.tastyhouse.domain.shared.geo.GeoPolygon;
import com.tastyhouse.ceoapplication.shop.port.out.ShopDeliveryAreaBulkDeleteResult;
import com.tastyhouse.ceoapplication.shop.port.out.ShopDeliveryAreaBulkResult;
import com.tastyhouse.ceoapplication.shop.port.in.ShopDeliveryAreaBulkCreateCommand;
import com.tastyhouse.ceoapplication.shop.port.in.ShopDeliveryAreaBulkDeleteCommand;
import com.tastyhouse.ceoapplication.shop.port.in.ShopDeliveryAreaCommandUseCase;
import com.tastyhouse.ceoapplication.shop.port.in.ShopDeliveryAreaCreateCommand;
import com.tastyhouse.ceoapplication.shop.port.in.ShopDeliveryAreaDeleteCommand;
import com.tastyhouse.ceoapplication.shop.port.in.ShopDeliveryAreaPolygonDeleteCommand;
import com.tastyhouse.ceoapplication.shop.port.in.ShopDeliveryAreaPolygonSaveCommand;
import com.tastyhouse.ceoapplication.shop.port.in.ShopDeliveryAreaRadiusApplyCommand;

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
 *
 * <p><b>변경이력</b>: {@code DELIVERY_AREA}·{@code DELIVERY_AREA_RADIUS}·{@code DELIVERY_AREA_POLYGON}
 * 기록은 변경 전 값을 추가 조회 없이 볼 수 있는 도메인 서비스들이 담당하고, 이 서비스는 변경 주체
 * ({@link ShopChangeActor})만 만들어 전달한다({@code ShopStatusCommandService}와 동일한 형태).
 */
@Service
@Transactional
public class ShopDeliveryAreaCommandService implements ShopDeliveryAreaCommandUseCase {

    private final ShopDeliveryAreaService shopDeliveryAreaService;
    private final ShopDeliveryAreaPolygonService shopDeliveryAreaPolygonService;
    private final ShopDeliveryAreaRadiusService shopDeliveryAreaRadiusService;
    private final ShopDeliveryAreaRepository shopDeliveryAreaRepository;
    private final AdminDongRepository adminDongRepository;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopDeliveryAreaCommandService(
        ShopDeliveryAreaService shopDeliveryAreaService,
        ShopDeliveryAreaPolygonService shopDeliveryAreaPolygonService,
        ShopDeliveryAreaRadiusService shopDeliveryAreaRadiusService,
        ShopDeliveryAreaRepository shopDeliveryAreaRepository,
        AdminDongRepository adminDongRepository,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopDeliveryAreaService = shopDeliveryAreaService;
        this.shopDeliveryAreaPolygonService = shopDeliveryAreaPolygonService;
        this.shopDeliveryAreaRadiusService = shopDeliveryAreaRadiusService;
        this.shopDeliveryAreaRepository = shopDeliveryAreaRepository;
        this.adminDongRepository = adminDongRepository;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public Long addDeliveryArea(ShopDeliveryAreaCreateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long adminDongId = command.adminDongId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopId targetShopId = ShopId.of(shopId);
        AdminDongId targetAdminDongId = AdminDongId.of(adminDongId);
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        return shopDeliveryAreaService.addArea(targetShopId, targetAdminDongId, actor);
    }

    /**
     * 배달가능지역을 삭제한다 — 삭제 대상 행에서 shopId를 역조회해 소유권을 먼저 검증한다.
     */
    @Override
    public void removeDeliveryArea(ShopDeliveryAreaDeleteCommand command) {
        Long ceoId = command.ceoId();
        Long deliveryAreaId = command.deliveryAreaId();

        ShopDeliveryArea deliveryArea = shopDeliveryAreaRepository.findById(deliveryAreaId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_DELIVERY_AREA_NOT_FOUND));
        shopOwnershipValidator.validateOwnership(ceoId, deliveryArea.getShopId().value());

        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopDeliveryAreaService.removeArea(deliveryAreaId, actor);
    }

    /**
     * 행정동을 일괄 추가한다. 이미 등록된 동은 건너뛰고, 없는 동이 섞이면 전체를 404로 막는다.
     */
    @Override
    public ShopDeliveryAreaBulkResult addDeliveryAreas(ShopDeliveryAreaBulkCreateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        List<Long> adminDongIds = command.adminDongIds();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopId targetShopId = ShopId.of(shopId);
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        return toBulkResult(shopDeliveryAreaService.addAreas(targetShopId, toAdminDongIds(adminDongIds), actor));
    }

    /**
     * 행정동을 일괄 삭제한다. 지역별 배달팁이 참조하는 동이 하나라도 섞이면 한 건도 지우지 않고 409다.
     */
    @Override
    public ShopDeliveryAreaBulkDeleteResult removeDeliveryAreas(ShopDeliveryAreaBulkDeleteCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        List<Long> adminDongIds = command.adminDongIds();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopId targetShopId = ShopId.of(shopId);
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        ShopDeliveryAreaService.BulkResult result = shopDeliveryAreaService.removeAreas(
            targetShopId, toAdminDongIds(adminDongIds), this::resolveRegionNames, actor
        );
        return new ShopDeliveryAreaBulkDeleteResult(
            result.requestedCount() - result.skippedCount(),
            result.totalCount()
        );
    }

    /**
     * 반경 안에 드는 행정동을 일괄 적용한다.
     *
     * <p>기준점은 <b>가게의 현재 좌표</b>다 — 소유권 검증이 반환한 도메인에서 얻으므로 추가 조회가 없다.
     */
    @Override
    public ShopDeliveryAreaBulkResult applyRadius(ShopDeliveryAreaRadiusApplyCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        int radiusMeters = command.radiusMeters();
        boolean replace = command.replace();

        Shop shop = shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopId targetShopId = ShopId.of(shopId);
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        return toBulkResult(shopDeliveryAreaRadiusService.applyRadius(
            targetShopId,
            shopLocationOf(shop),
            radiusMeters,
            replace,
            this::resolveRegionNames,
            actor
        ));
    }

    /**
     * 배달지역 도형을 저장하고 그 자리에서 행정동으로 환산한다(전체 교체).
     */
    @Override
    public void savePolygon(ShopDeliveryAreaPolygonSaveCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();

        Shop shop = shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopId targetShopId = ShopId.of(shopId);
        GeoPolygon polygon = ShopDeliveryAreaGeoMapper.toPolygon(command.rings());
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopDeliveryAreaPolygonService.savePolygon(
            targetShopId,
            polygon,
            shopLocationOf(shop),
            this::resolveRegionNames,
            actor
        );
    }

    /**
     * 배달지역 도형과 그로부터 파생된 행정동을 삭제한다. 직접 등록한 행정동은 남는다.
     */
    @Override
    public void deletePolygon(ShopDeliveryAreaPolygonDeleteCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopId targetShopId = ShopId.of(shopId);
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopDeliveryAreaPolygonService.deletePolygon(targetShopId, this::resolveRegionNames, actor);
    }

    /**
     * 가게의 현재 좌표를 도메인 기하 타입으로 승격한다. 좌표가 없으면 7km 상한의 기준점이 없어 도형·반경
     * 설정이 성립하지 않으므로 명확히 막는다 — 좌표 없이 진행하면 NPE로 500이 난다.
     */
    private GeoPoint shopLocationOf(Shop shop) {
        if (shop.getLatitude() == null || shop.getLongitude() == null) {
            throw new BusinessException(
                ErrorCode.SHOP_DELIVERY_AREA_RADIUS_EXCEEDED,
                "가게 좌표가 등록돼 있지 않아 배달지역을 설정할 수 없습니다."
            );
        }
        return GeoPoint.of(shop.getLatitude(), shop.getLongitude());
    }

    /**
     * 차단 메시지에 나열할 행정동 이름을 조회한다.
     *
     * <p>이 조회가 write 포트({@code AdminDongRepository})를 쓰는 이유는 이 클래스가 명령 경로이기
     * 때문이다 — CommandService가 infra query DAO를 주입하면 CQRS 교차 주입 금지 규칙에 걸린다.
     */
    private List<String> resolveRegionNames(Collection<AdminDongId> adminDongIds) {
        return adminDongRepository.findAllByIds(adminDongIds).stream()
            .map(AdminDong::fullName)
            .toList();
    }

    private ShopDeliveryAreaBulkResult toBulkResult(ShopDeliveryAreaService.BulkResult result) {
        return new ShopDeliveryAreaBulkResult(
            result.requestedCount(),
            result.addedCount(),
            result.skippedCount(),
            result.totalCount()
        );
    }

    private static List<AdminDongId> toAdminDongIds(List<Long> adminDongIds) {
        return adminDongIds.stream().map(AdminDongId::of).toList();
    }
}
