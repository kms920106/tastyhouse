package com.tastyhouse.domain.shop.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.ScheduledOrderPolicy;
import com.tastyhouse.domain.shop.model.ScheduledOrderSlot;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.repository.ShopDeliveryAreaRepository;
import com.tastyhouse.domain.shop.repository.ShopDeliveryTipRepository;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shared.geo.GeoDistance;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

/**
 * 주문 접수가 가게에 대해 물어보는 것들을 모은 파사드(도메인 서비스, shop 컨텍스트 소유).
 *
 * <p>가게 로드·주문가능 검증·최소주문금액 검증·배달가능지역 판정·거리 산출·배달팁 설정 조회를 한데 모아,
 * {@code OrderPlacementService}가 shop의 모델·리포지토리를 전혀 몰라도 주문을 접수할 수 있게 한다.
 *
 * <p><b>왜 파사드인가</b>: 과거 order는 {@code Shop} 애그리거트와 {@code ShopRepository}·
 * {@code ShopDeliveryAreaRepository}·{@code ShopDeliveryTipRepository} 3개를 직접 주입해, 배달가능지역
 * 판정("등록 0건이면 검사 생략")과 배달팁 입력 5종 조립을 order 안에서 수행했다. 이것들은 전부 가게의
 * 정책이므로 shop이 판정해야 하고, 특히 <b>지역 미등록 가게를 열어 두는 규칙</b>이 order에 복제돼 있으면
 * shop이 정책을 바꿀 때 주문 경로만 낡은 규칙으로 남는다.
 *
 * <p><b>이미 서비스 경유이던 것은 그대로 재사용한다</b> — 주문가능 검증은
 * {@link ShopOrderAvailabilityService}, 배달팁 산출은 {@link ShopDeliveryTipCalculator}, 예약 슬롯은
 * {@link ScheduledOrderSlotService}가 계속 담당하고 이 파사드는 그 앞단의 로드·조립만 흡수한다.
 *
 * <p><b>동작을 바꾸지 않는다</b> — 검증 순서와 에러코드는 이관 전과 동일하며, 전부 같은 트랜잭션 안의
 * 동기 호출이다(이벤트로 바꾸지 않는다).
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code ShopDomainConfig}가 담당한다.
 */
public class ShopOrderContextService {

    private final ShopRepository shopRepository;
    private final ShopDeliveryAreaRepository shopDeliveryAreaRepository;
    private final ShopDeliveryTipRepository shopDeliveryTipRepository;
    private final ShopOrderAvailabilityService shopOrderAvailabilityService;
    private final ShopDeliveryTipCalculator shopDeliveryTipCalculator;
    private final ScheduledOrderSlotService scheduledOrderSlotService;

    public ShopOrderContextService(
        ShopRepository shopRepository,
        ShopDeliveryAreaRepository shopDeliveryAreaRepository,
        ShopDeliveryTipRepository shopDeliveryTipRepository,
        ShopOrderAvailabilityService shopOrderAvailabilityService,
        ShopDeliveryTipCalculator shopDeliveryTipCalculator,
        ScheduledOrderSlotService scheduledOrderSlotService
    ) {
        this.shopRepository = shopRepository;
        this.shopDeliveryAreaRepository = shopDeliveryAreaRepository;
        this.shopDeliveryTipRepository = shopDeliveryTipRepository;
        this.shopOrderAvailabilityService = shopOrderAvailabilityService;
        this.shopDeliveryTipCalculator = shopDeliveryTipCalculator;
        this.scheduledOrderSlotService = scheduledOrderSlotService;
    }

    /**
     * 1단계 — 가게를 로드하고 지금 이 주문유형으로 접수 가능한지 검증한다.
     *
     * <p>회원 경로이므로 {@code findVisibleById}를 쓴다 — 폐업·노출정지 가게는 404가 되어 딥링크 진입이
     * 차단된다(장바구니가 없는 이 서비스에서 서버 측 유일한 게이트다). 영업상태·유형배정·유형중지 위반은
     * 각각 다른 400 에러코드로 거절된다.
     *
     * <p>가게 애그리거트를 밖으로 내보내지 않으려고 <b>핸들</b>을 돌려준다 — 이후 단계들이 이 핸들을
     * 되돌려주면 파사드가 가게를 다시 읽지 않고 재사용한다.
     */
    public OrderableShop loadOrderableShop(ShopId shopId, OrderMethod orderMethod, LocalDateTime at) {
        Shop shop = shopRepository.findVisibleById(shopId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));
        shopOrderAvailabilityService.validateOrderable(shop, orderMethod, at);
        return new OrderableShop(shop);
    }

    /**
     * 4단계 — 가게 최소주문금액을 검증한다.
     *
     * <p>기준 금액은 상품 할인까지 반영하되 <b>배달팁은 포함하지 않은</b> 값이다 — 포함하면 팁이 비싼
     * 가게일수록 최소주문 문턱이 낮아지는 역설이 생긴다. 판정·면제 조건은 {@code Shop#validateMinOrderAmount}에 있다.
     */
    public void validateMinOrderAmount(
        OrderableShop shop,
        OrderMethod orderMethod,
        int orderAmountAfterProductDiscount
    ) {
        shop.value().validateMinOrderAmount(orderMethod, orderAmountAfterProductDiscount);
    }

    /**
     * 5단계 — 배달지가 가게 배달가능지역에 드는지 검증하고, 거리와 배달팁을 산출한다.
     *
     * <p><b>배달가능지역을 하나도 등록하지 않은 가게는 검사를 생략한다.</b> 기존 데이터의 모든 가게가
     * 0건이므로 항상 거절하면 배포 즉시 전 가게의 배달 주문이 막힌다. "정보를 안 넣은 것을 닫힌 것으로
     * 보지 않는다"는 이 도메인의 원칙(영업시간 미입력을 준비중으로 오판하지 않는 것)과도 일치한다.
     *
     * <p>배달팁 기준 시각은 <b>주문 접수 시점</b>이다. 주문서 진입 시점과 시간별 팁 구간이 달라지면
     * 이후 금액 대조에서 거절되고 프론트가 재견적 후 재시도한다.
     *
     * <p><b>공휴일 여부는 이미 판정된 값으로 받는다</b> — 공휴일 캘린더는 {@code holiday} 컨텍스트
     * 소유이므로 shop이 그 서비스를 직접 호출하면 컨텍스트 경계를 위반한다. 순수 계산기
     * {@link ShopDeliveryTipCalculator}가 해석된 {@code boolean}을 받는 것과 같은 이유이며, 판정은
     * 호출부가 수행한다.
     *
     * @param address       배달 목적지. 좌표는 <b>저장된 주소에서만</b> 읽은 값이어야 한다 —
     *                      클라이언트가 보낸 좌표를 그대로 넘기면 가짜 좌표로 거리별 팁을 0원까지 낮출 수 있다
     * @param publicHoliday 접수 시각이 공휴일인지 — {@code holiday} 컨텍스트가 판정한 값
     */
    public ShopDeliveryResolution resolveDelivery(
        OrderableShop shop,
        ShopId shopId,
        DeliveryDestinationSpec address,
        OrderMethod orderMethod,
        int orderAmountAfterProductDiscount,
        LocalDateTime orderedAt,
        boolean publicHoliday
    ) {
        validateDeliveryArea(shopId, address.adminDongId());

        double meters = GeoDistance.distanceMeters(
            shop.value().getLatitude(), shop.value().getLongitude(), address.latitude(), address.longitude()
        );

        ShopDeliveryTipBreakdown breakdown = shopDeliveryTipCalculator.calculate(ShopDeliveryTipContext.of(
            orderMethod,
            orderAmountAfterProductDiscount,
            meters,
            address.adminDongId(),
            orderedAt,
            publicHoliday,
            shopDeliveryTipRepository.findSettingByShopId(shopId).orElse(null),
            shopDeliveryTipRepository.findTiersByShopId(shopId),
            shopDeliveryTipRepository.findRegionTipsByShopId(shopId),
            shopDeliveryTipRepository.findScheduleTipsByShopId(shopId),
            shopDeliveryTipRepository.findHolidayTipByShopId(shopId).orElse(null)
        ));

        return new ShopDeliveryResolution((int) Math.round(meters), breakdown);
    }

    /**
     * 5.5단계 — 수령 예약시간 슬롯을 재계산해 확정한다(즉시 주문이면 호출하지 않는다).
     *
     * <p>클라이언트가 보낸 시각을 신뢰하지 않고 서버가 슬롯을 재계산해 대조한다 — 배달팁 금액 대조와
     * 같은 원칙이며, 대조는 {@link ScheduledOrderSlotService#resolveSlot}가 수행한다.
     *
     * <p>예약 자체가 불가한 두 경우를 먼저 거른다 — 가게가 예약주문을 끄면
     * {@code SHOP_SCHEDULED_ORDER_DISABLED}, 그 주문유형이 예약을 지원하지 않으면
     * {@code ORDER_SCHEDULE_METHOD_NOT_SUPPORTED}다.
     */
    public ScheduledOrderSlot resolveScheduledSlot(
        OrderableShop shop,
        ShopId shopId,
        OrderMethod orderMethod,
        LocalDateTime scheduledAt,
        LocalDateTime at
    ) {
        if (!shop.value().isScheduledOrderEnabled()) {
            throw new BusinessException(ErrorCode.SHOP_SCHEDULED_ORDER_DISABLED);
        }
        if (!ScheduledOrderPolicy.supports(orderMethod)) {
            throw new BusinessException(ErrorCode.ORDER_SCHEDULE_METHOD_NOT_SUPPORTED,
                ErrorCode.ORDER_SCHEDULE_METHOD_NOT_SUPPORTED.getDefaultMessage() + ": " + orderMethod);
        }

        return scheduledOrderSlotService.resolveSlot(shopId, orderMethod, scheduledAt, at);
    }

    private void validateDeliveryArea(ShopId shopId, AdminDongId adminDongId) {
        if (shopDeliveryAreaRepository.countByShopId(shopId) == 0) {
            return;
        }
        if (adminDongId == null
            || !shopDeliveryAreaRepository.existsByShopIdAndAdminDongId(shopId, adminDongId)) {
            throw new BusinessException(ErrorCode.ORDER_DELIVERY_AREA_NOT_COVERED);
        }
    }

    /**
     * 주문 접수 가능이 확인된 가게의 핸들.
     *
     * <p>가게 애그리거트를 감싸 <b>타 컨텍스트로 새어 나가지 않게</b> 한다 — 값 접근자가
     * package-private이라 shop 컨텍스트 밖에서는 안을 열어볼 수 없고, 호출부는 이 핸들을 이후 단계에
     * 되돌려주는 토큰으로만 쓴다. 그 덕분에 가게를 한 번만 읽고 재사용하면서도 order가 {@code Shop}을
     * import하지 않는다.
     */
    public static final class OrderableShop {

        private final Shop shop;

        private OrderableShop(Shop shop) {
            this.shop = shop;
        }

        Shop value() {
            return this.shop;
        }
    }

    /**
     * 배달 목적지 입력 — 검증·산출에 필요한 값만 담는다.
     *
     * <p>{@code MemberDeliveryAddress}를 그대로 받으면 shop이 member 내부를 알게 되므로, 호출부가
     * 필요한 값만 옮겨 담아 넘긴다. 두 좌표가 같은 {@code BigDecimal}이라 순서를 바꿔도 컴파일되므로
     * 조립 시 자리를 대조한다.
     */
    public record DeliveryDestinationSpec(
        AdminDongId adminDongId,
        BigDecimal latitude,
        BigDecimal longitude
    ) {

        public static DeliveryDestinationSpec of(
            AdminDongId adminDongId,
            BigDecimal latitude,
            BigDecimal longitude
        ) {
            return new DeliveryDestinationSpec(adminDongId, latitude, longitude);
        }
    }
}
