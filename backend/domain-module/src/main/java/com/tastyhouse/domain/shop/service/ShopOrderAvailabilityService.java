package com.tastyhouse.domain.shop.service;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shop.model.OrderMethod;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopOrderMethod;
import com.tastyhouse.domain.shop.repository.ShopDetailRepository;

/**
 * 주문 접수 게이트 — "지금 이 가게에 이 주문유형으로 주문할 수 있는가"를 검증한다(도메인 서비스).
 *
 * <p>주문 접수({@code OrderPlacementService})와 예약 생성({@code ReservationBookingService})이 <b>같은
 * 규칙</b>을 써야 하므로 한 곳에 둔다 — 각자 구현하면 한쪽만 고쳐져 "주문은 막히는데 예약은 되는" 식으로
 * 갈린다. 가게 존재 확인({@code findVisibleById})은 호출부가 이미 수행하므로 여기서 반복하지 않는다.
 *
 * <p>검증 3종을 순서대로 수행하며 첫 위반에서 즉시 실패한다:
 * <ol>
 *   <li>가게 주문가능 상태 → {@link ErrorCode#SHOP_NOT_ORDERABLE}</li>
 *   <li>그 유형이 가게에 배정됨 → {@link ErrorCode#SHOP_ORDER_METHOD_NOT_SUPPORTED}</li>
 *   <li>그 유형에 활성 임시중지 없음 → {@link ErrorCode#SHOP_ORDER_METHOD_SUSPENDED}</li>
 * </ol>
 *
 * <p>1번과 3번을 나눠 부르는 이유는 <b>사유를 구분해 내려주기 위함</b>이다. 유형 판정 한 번으로 둘을
 * 합치면 "가게가 닫혔는지 그 유형만 중지됐는지"를 클라이언트가 구분할 수 없다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code ShopDomainConfig}가 담당한다.
 */
public class ShopOrderAvailabilityService {

    private final ShopOperatingStatusService shopOperatingStatusService;
    private final ShopDetailRepository shopDetailRepository;

    public ShopOrderAvailabilityService(
        ShopOperatingStatusService shopOperatingStatusService,
        ShopDetailRepository shopDetailRepository
    ) {
        this.shopOperatingStatusService = shopOperatingStatusService;
        this.shopDetailRepository = shopDetailRepository;
    }

    /**
     * 주문 접수 가능 여부를 검증한다. 위반 시 사유에 맞는 {@link BusinessException}(400)을 던진다.
     *
     * <p><b>{@code at}은 "지금"이 아닐 수 있다</b> — 예약 생성은 예약 슬롯 시각을 넘긴다. 지금이
     * 휴게시간이라고 3시간 뒤 예약을 막으면 안 되기 때문이다. 반대로 폐업·노출정지는 시각과 무관하므로
     * 호출부가 {@code findVisibleById}로 로드해 넘긴 가게이므로 이 메서드에 도달하기 전에 걸러진다.
     *
     * <p><b>가게를 식별자가 아니라 애그리거트로 받는 이유</b>: 식별자를 받아 내부에서 다시 읽으면
     * {@code findById}가 되어 호출부의 회원 노출용 조회를 무효화하고, 폐업·노출정지 가게가 404가 아니라
     * 400으로 거절되어 응답 계약이 갈린다. 로드한 가게를 그대로 받으면 그 경로 자체가 생기지 않는다.
     *
     * @param shop        대상 가게. 회원 경로는 {@code findVisibleById}로 로드해 넘긴다
     * @param orderMethod 주문유형
     * @param at          판정 기준 시각
     */
    public void validateOrderable(Shop shop, OrderMethod orderMethod, LocalDateTime at) {
        ShopOrderMethodAvailability availability =
            shopOperatingStatusService.findOrderAvailability(shop, orderMethod, at);

        ShopOperatingStatusResult shopStatus = availability.shopWide();
        if (!shopStatus.isOpen()) {
            throw new BusinessException(ErrorCode.SHOP_NOT_ORDERABLE,
                ErrorCode.SHOP_NOT_ORDERABLE.getDefaultMessage()
                    + ": " + shopStatus.unavailableReason().getDisplayName());
        }

        if (!isAssigned(shop.getId(), orderMethod)) {
            throw new BusinessException(ErrorCode.SHOP_ORDER_METHOD_NOT_SUPPORTED,
                ErrorCode.SHOP_ORDER_METHOD_NOT_SUPPORTED.getDefaultMessage()
                    + ": " + orderMethod.getDisplayName());
        }

        ShopOperatingStatusResult methodStatus = availability.orderMethod();
        if (!methodStatus.isOpen()) {
            // 사유를 함께 담는다 — 가게 전체 판정을 통과한 뒤에도 불가인 사유는 현재 SUSPENDED뿐이지만,
            // 유형별 운영시간처럼 유형 범위 사유가 늘면 이 문구가 그것을 그대로 드러낸다.
            throw new BusinessException(ErrorCode.SHOP_ORDER_METHOD_SUSPENDED,
                ErrorCode.SHOP_ORDER_METHOD_SUSPENDED.getDefaultMessage()
                    + ": " + orderMethod.getDisplayName()
                    + " (" + methodStatus.unavailableReason().getDisplayName() + ")");
        }
    }

    private boolean isAssigned(Long shopId, OrderMethod orderMethod) {
        return shopDetailRepository.findOrderMethodsByShopId(shopId).stream()
            .map(ShopOrderMethod::getOrderMethod)
            .anyMatch(assigned -> assigned == orderMethod);
    }
}
