package com.tastyhouse.domain.shop.service;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.order.vo.OrderSchedule;
import com.tastyhouse.domain.shop.model.OrderMethod;
import com.tastyhouse.domain.shop.model.ScheduledOrderSlot;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopBreakTime;
import com.tastyhouse.domain.shop.model.ShopBusinessHour;
import com.tastyhouse.domain.shop.model.ShopClosedDay;
import com.tastyhouse.domain.shop.model.ShopOrderMethod;
import com.tastyhouse.domain.shop.model.ShopSuspension;
import com.tastyhouse.domain.shop.model.ShopTemporaryClosure;
import com.tastyhouse.domain.shop.repository.ShopDetailRepository;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.repository.ShopSuspensionRepository;
import com.tastyhouse.domain.shop.repository.ShopTemporaryClosureRepository;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 예약 가능 수령시간 슬롯 조회·확정 오케스트레이션(도메인 서비스).
 *
 * <p>슬롯 판정은 가게·영업시간·휴게시간·정기휴무·임시휴무·임시중지 <b>여섯 애그리거트</b>를 모두 읽어야
 * 가능하다({@link ShopOperatingStatusService}와 같은 조회 셋). 이 서비스는 그 조회·조립만 담당하고 판정
 * 규칙 자체는 순수 계산기 {@link ScheduledOrderSlotCalculator}에 위임한다. 규칙이 소비 액터(web 슬롯
 * 조회·주문 접수)와 무관하게 동일해야 하므로 도메인 계층에 둔다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code ShopDomainConfig}가 담당한다.
 */
public class ScheduledOrderSlotService {

    private final ShopRepository shopRepository;
    private final ShopDetailRepository shopDetailRepository;
    private final ShopTemporaryClosureRepository shopTemporaryClosureRepository;
    private final ShopSuspensionRepository shopSuspensionRepository;
    private final ScheduledOrderSlotCalculator scheduledOrderSlotCalculator;

    public ScheduledOrderSlotService(
        ShopRepository shopRepository,
        ShopDetailRepository shopDetailRepository,
        ShopTemporaryClosureRepository shopTemporaryClosureRepository,
        ShopSuspensionRepository shopSuspensionRepository,
        ScheduledOrderSlotCalculator scheduledOrderSlotCalculator
    ) {
        this.shopRepository = shopRepository;
        this.shopDetailRepository = shopDetailRepository;
        this.shopTemporaryClosureRepository = shopTemporaryClosureRepository;
        this.shopSuspensionRepository = shopSuspensionRepository;
        this.scheduledOrderSlotCalculator = scheduledOrderSlotCalculator;
    }

    /**
     * 예약 가능한 슬롯 목록을 시작 시각 오름차순으로 조회한다.
     *
     * <p>예약주문 미운영·미지원 주문방식·영업시간 미등록 등 예약할 수 없는 상태는 예외가 아니라 <b>빈
     * 목록</b>이다 — 소비 API가 404가 아니라 {@code available:false}로 내려주기 위함이다.
     *
     * @throws ResourceNotFoundException 가게가 없는 경우
     */
    public List<ScheduledOrderSlot> findAvailableSlots(ShopId shopId, OrderMethod orderMethod, LocalDateTime now) {
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));

        return scheduledOrderSlotCalculator.calculate(buildContext(shop, shopId, orderMethod, now));
    }

    /**
     * 클라이언트가 보낸 수령 예약 시각을 <b>서버가 슬롯을 재계산해 대조</b>한 뒤 스냅샷으로 확정한다.
     *
     * <p>클라이언트 값을 그대로 믿지 않는 이유는 배달팁 금액 대조와 같다 — 주문서 진입 시점과 결제 시점
     * 사이에 영업시간·임시중지가 바뀌거나 경계가 지나면 그 시각은 더 이상 예약 가능하지 않다. 30분 단위를
     * 벗어난 임의 시각·영업시간 밖 시각도 어느 슬롯과도 일치하지 않아 같은 경로로 거절된다.
     *
     * @throws BusinessException 요청 시각이 현재 유효 슬롯 목록에 없는 경우
     *                           ({@link ErrorCode#ORDER_SCHEDULED_AT_UNAVAILABLE})
     */
    public OrderSchedule resolveSlot(
        ShopId shopId,
        OrderMethod orderMethod,
        LocalDateTime scheduledAt,
        LocalDateTime now
    ) {
        return findAvailableSlots(shopId, orderMethod, now).stream()
            .filter(slot -> slot.matches(scheduledAt))
            .findFirst()
            .map(OrderSchedule::of)
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_SCHEDULED_AT_UNAVAILABLE,
                ErrorCode.ORDER_SCHEDULED_AT_UNAVAILABLE.getDefaultMessage() + ": " + scheduledAt));
    }

    private ScheduledOrderSlotContext buildContext(
        Shop shop,
        ShopId shopId,
        OrderMethod orderMethod,
        LocalDateTime now
    ) {
        Long rawShopId = shopId.value();
        List<ShopBusinessHour> businessHours = shopDetailRepository.findBusinessHoursByShopId(rawShopId);
        List<ShopBreakTime> breakTimes = shopDetailRepository.findBreakTimesByShopId(rawShopId);
        List<ShopClosedDay> closedDays = shopDetailRepository.findClosedDaysByShopId(rawShopId);
        List<ShopTemporaryClosure> temporaryClosures = shopTemporaryClosureRepository.findByShopId(rawShopId);
        List<ShopSuspension> suspensions = shopSuspensionRepository.findByShopId(rawShopId);
        List<ShopOrderMethod> shopOrderMethods = shopDetailRepository.findOrderMethodsByShopId(rawShopId);

        return ScheduledOrderSlotContext.of(
            shop,
            orderMethod,
            now,
            businessHours,
            breakTimes,
            closedDays,
            temporaryClosures,
            suspensions,
            shopOrderMethods
        );
    }
}
