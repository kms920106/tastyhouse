package com.tastyhouse.domain.order.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.tastyhouse.domain.coupon.service.CouponIssueService;
import com.tastyhouse.domain.coupon.service.CouponUseResult;
import com.tastyhouse.domain.coupon.vo.MemberCouponId;
import com.tastyhouse.domain.holiday.service.PublicHolidayCalendar;
import com.tastyhouse.domain.member.service.MemberDeliveryAddressService;
import com.tastyhouse.domain.member.service.OrdererLookupService;
import com.tastyhouse.domain.member.service.OrdererSnapshot;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.model.Order;
import com.tastyhouse.domain.order.model.OrderProduct;
import com.tastyhouse.domain.order.model.OrderProductOption;
import com.tastyhouse.domain.order.model.OrderStatus;
import com.tastyhouse.domain.order.repository.OrderProductOptionRepository;
import com.tastyhouse.domain.order.repository.OrderProductRepository;
import com.tastyhouse.domain.order.repository.OrderRepository;
import com.tastyhouse.domain.order.vo.OrderDeliveryDestination;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.order.vo.OrderSchedule;
import com.tastyhouse.domain.point.service.PointLedgerService;
import com.tastyhouse.domain.product.service.OrderLineOptionSelection;
import com.tastyhouse.domain.product.service.OrderLineSelection;
import com.tastyhouse.domain.product.service.OrderProductOptionSnapshot;
import com.tastyhouse.domain.product.service.OrderProductSnapshot;
import com.tastyhouse.domain.product.service.OrderProductValidationService;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.ScheduledOrderSlot;
import com.tastyhouse.domain.shop.service.ShopDeliveryResolution;
import com.tastyhouse.domain.shop.service.ShopDeliveryTipBreakdown;
import com.tastyhouse.domain.shop.service.ShopOrderContextService;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 주문 접수 불변식(도메인 서비스).
 *
 * <p>주문 한 건의 접수는 {@code Order} 헤더 · 상품 라인({@code OrderProduct}) · 라인 옵션
 * ({@code OrderProductOption}) 세 애그리거트를 한 트랜잭션에서 함께 만들고, 그 과정에서 계산한 금액을
 * 헤더에 되반영해야 하는 원자 연산이다. 세 애그리거트 중 하나라도 빠지면 주문이 반쪽으로 저장되고,
 * 금액 되반영이 빠지면 결제 금액이 0원인 주문이 남는다. 여기에 쿠폰 사용({@link CouponIssueService}) ·
 * 포인트 차감({@link PointLedgerService})까지 같은 트랜잭션에 묶이는 크로스 애그리거트 불변식
 * 오케스트레이션(분류 C)이므로 도메인 계층에 둔다.
 *
 * <p><b>타 컨텍스트는 전부 그 컨텍스트의 서비스를 경유한다</b> — 상품·옵션 검증은
 * {@link OrderProductValidationService}(product 소유), 가게 로드·주문가능·최소주문금액·배달지역·배달팁·
 * 예약슬롯은 {@link ShopOrderContextService}(shop 소유), 주문자 조회는 {@link OrdererLookupService},
 * 배달 주소 로드·소유권 검증은 {@link MemberDeliveryAddressService}(둘 다 member 소유)가 담당한다.
 * 과거 이 서비스는 외부 컨텍스트 6개에서 모델·리포지토리를 직접 주입해 26개를 import했는데, 그러면
 * 각 컨텍스트의 규칙(판매중지 판정·배달지역 미등록 처리·주소 소유권)이 주문 안에서 재구현되어 소유
 * 컨텍스트가 정책을 바꿀 때 주문 경로만 낡은 규칙으로 남는다. 지금 남은 타 컨텍스트 의존은
 * <b>서비스와 그 결과 record</b>뿐이며 모델·리포지토리 직접 import는 0건이다.
 *
 * <p><b>이관해도 트랜잭션 경계는 그대로다</b> — 전부 같은 트랜잭션 안의 동기 호출이며(이벤트로 바꾸지
 * 않는다), 검증 순서·에러코드·응답 계약도 이관 전과 동일하다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code OrderDomainConfig}가 담당한다. 트랜잭션 경계는 이 서비스를 호출하는
 * 소비 모듈의 command 서비스(web-api {@code OrderCommandService})가 선언한다.
 *
 * <p>주문 접수는 도메인 이벤트를 발행하지 않는다 — 과거 {@code OrderCreatedEvent}를 발행했으나 수신
 * 리스너가 없는 no-op이어서 P9(도메인 이벤트 정비)에서 제거했다. 접수 이후 비동기 후처리(알림·집계)가
 * 필요해지면 이 메서드 말미에 발행을 다시 추가하면 된다.
 *
 * <p>도메인 모델이 순수 POJO라 더티 체킹이 없으므로 변경 후 명시적으로 {@code save}를 호출한다
 * (헤더는 신규 저장 후 금액 갱신으로 2회, 상품 라인은 신규 저장 후 가격 갱신으로 2회 저장한다).
 *
 * <p>반환은 생성된 주문의 식별자({@link OrderId})만이다 — 응답 조립(가게명·상품 라인·결제 요약)은
 * 커밋 이후 소비 모듈의 {@code OrderQueryService}가 infra query DAO로 재조회해 담당한다(CQRS 분리).
 */
public class OrderPlacementService {

    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final OrderProductOptionRepository orderProductOptionRepository;
    private final OrderProductValidationService orderProductValidationService;
    private final ShopOrderContextService shopOrderContextService;
    private final OrdererLookupService ordererLookupService;
    private final MemberDeliveryAddressService memberDeliveryAddressService;
    private final CouponIssueService couponIssueService;
    private final PointLedgerService pointLedgerService;
    private final PublicHolidayCalendar publicHolidayCalendar;

    /**
     * @param orderRepository               주문 헤더 저장·재저장(금액 확정 반영)
     * @param orderProductRepository        상품 라인 저장·가격 갱신
     * @param orderProductOptionRepository  라인 옵션 저장
     * @param orderProductValidationService 상품·옵션 존재·판매중지 검증과 주문 라인 스냅샷 산출(product 소유)
     * @param shopOrderContextService       가게 로드·주문가능·최소주문금액·배달지역·배달팁·예약슬롯(shop 소유)
     * @param ordererLookupService          주문자 존재 확인과 이름·연락처 스냅샷(member 소유)
     * @param memberDeliveryAddressService  배달 주소 로드와 소유권 검증(member 소유). 좌표는 저장된
     *                                      주소에서만 읽는다 — 위조 방지
     * @param couponIssueService            쿠폰 사용 처리(할인액 산출·상태 전이)
     * @param pointLedgerService            포인트 차감 원장 기록
     * @param publicHolidayCalendar         접수 시각의 공휴일 여부 판정. shop이 이 캘린더를 직접 부르면
     *                                      컨텍스트 경계를 위반하므로, 판정 결과만 배달팁 산출에 넘긴다
     */
    public OrderPlacementService(
        OrderRepository orderRepository,
        OrderProductRepository orderProductRepository,
        OrderProductOptionRepository orderProductOptionRepository,
        OrderProductValidationService orderProductValidationService,
        ShopOrderContextService shopOrderContextService,
        OrdererLookupService ordererLookupService,
        MemberDeliveryAddressService memberDeliveryAddressService,
        CouponIssueService couponIssueService,
        PointLedgerService pointLedgerService,
        PublicHolidayCalendar publicHolidayCalendar
    ) {
        this.orderRepository = orderRepository;
        this.orderProductRepository = orderProductRepository;
        this.orderProductOptionRepository = orderProductOptionRepository;
        this.orderProductValidationService = orderProductValidationService;
        this.shopOrderContextService = shopOrderContextService;
        this.ordererLookupService = ordererLookupService;
        this.memberDeliveryAddressService = memberDeliveryAddressService;
        this.couponIssueService = couponIssueService;
        this.pointLedgerService = pointLedgerService;
        this.publicHolidayCalendar = publicHolidayCalendar;
    }

    /**
     * 주문을 접수한다(10단계) — <b>가게 존재·주문가능 검증</b> → 회원 존재 확인 → 주문 헤더 생성 →
     * 상품 라인·옵션 생성과 금액 집계 →
     * 가게 최소주문금액 검증 → <b>배달 목적지 확정과 배달팁 산출</b> → <b>수령 예약시간 확정</b> →
     * 쿠폰·포인트 사용 → 금액 대조 검증 → 헤더 금액 반영.
     *
     * <p>수령 예약시간(5.5단계)은 <b>금액에 영향을 주지 않는다</b> — 예약주문이든 즉시 주문이든 배달팁·
     * 최소주문금액·쿠폰/포인트 계산이 동일하다.
     *
     * <p><b>주문가능 검증(1단계)은 상품·금액 계산 전에 수행한다</b> — 거절될 주문에 조회·계산 자원을 쓰지
     * 않기 위함이다. 폐업·노출정지 가게는 404가 되고(서버 측 유일한 게이트이므로 장바구니 없는 이
     * 서비스에서 반드시 막아야 한다), 영업상태·유형배정·유형중지 위반은 각각 다른 400 에러코드로 거절된다.
     *
     * <p><b>이 검증의 기준 시각은 "지금"이다</b>(예약 생성이 슬롯 시각으로 판정하는 것과 다르다). 예약주문
     * (수령시간 지정)의 <b>미래 시각 판정은 5.5단계</b>가 담당한다 — 그 슬롯 재계산이 같은 영업상태
     * 계산기에 주문유형과 미래 시각을 함께 넘기므로, 수령 시각에 걸린 유형별 중지·휴게시간은 그쪽에서
     * 걸러진다. 즉 두 시각을 각자 맞는 단계에서 본다.
     *
     * <p>가게 최소주문금액 검증은 상품 할인까지 반영한 금액을 기준으로 쿠폰·포인트 사용 <b>전에</b> 수행한다.
     * 이 검증은 쿠폰 자체의 최소주문금액 검증({@code Coupon#validateMinOrderAmount})과 별개이며,
     * <b>배달팁은 그 판정 기준에 포함되지 않는다</b> — 포함하면 팁이 비싼 가게일수록 최소주문 문턱이
     * 낮아지는 역설이 생긴다.
     *
     * <p>쿠폰·포인트의 할인 기준과 상한에도 배달팁은 <b>포함되지 않는다</b> — 팁만 남기고 상품값을 0으로
     * 만드는 조합을 차단하고, 팁은 프로모션 재원과 성격이 다르기 때문이다.
     *
     * @return 생성된 주문 식별자
     */
    public OrderId place(MemberId memberId, OrderPlacement placement) {
        ShopId shopId = ShopId.of(placement.shopId());
        ShopOrderContextService.OrderableShop shop = shopOrderContextService.loadOrderableShop(
            shopId, placement.orderMethod(), LocalDateTime.now()
        );

        OrdererSnapshot orderer = ordererLookupService.findOrderer(memberId);

        Order order = Order.of(
            memberId,
            shopId,
            generateOrderNumber(),
            placement.orderMethod(),
            OrderStatus.PENDING,
            orderer.fullName(),
            orderer.phoneNumber(),
            orderer.username(),
            0, 0, 0, 0, 0, 0, 0, OrderDeliveryDestination.none(), OrderSchedule.none(), null, 0, 0
        );
        Order savedOrder = orderRepository.save(order);

        List<OrderProductSnapshot> snapshots = orderProductValidationService.validate(toSelections(placement), LocalDateTime.now());

        int totalProductAmount = 0;
        int productDiscountAmount = 0;

        for (OrderProductSnapshot snapshot : snapshots) {
            OrderProduct orderProduct = OrderProduct.of(
                savedOrder.getOrderId(),
                snapshot.productId(),
                snapshot.name(),
                snapshot.representativeImageFileId(),
                snapshot.quantity(),
                snapshot.originalPrice(),
                snapshot.discountPrice(),
                0, 0
            );
            OrderProduct savedOrderProduct = orderProductRepository.save(orderProduct);

            int totalOptionPrice = saveSelectedOptions(savedOrderProduct, snapshot);

            int itemTotal = (snapshot.effectivePrice() + totalOptionPrice) * snapshot.quantity();
            int itemDiscount = snapshot.discountPrice() != null
                ? (snapshot.originalPrice() - snapshot.discountPrice()) * snapshot.quantity()
                : 0;

            savedOrderProduct.updatePrices(totalOptionPrice, itemTotal);
            orderProductRepository.save(savedOrderProduct);

            totalProductAmount += snapshot.originalPrice() * snapshot.quantity()
                + totalOptionPrice * snapshot.quantity();
            productDiscountAmount += itemDiscount;
        }

        int orderAmountAfterProductDiscount = totalProductAmount - productDiscountAmount;
        shopOrderContextService.validateMinOrderAmount(
            shop, placement.orderMethod(), orderAmountAfterProductDiscount
        );

        DeliveryTipResolution deliveryTip = resolveDeliveryTip(shop, shopId, memberId, placement,
            orderAmountAfterProductDiscount);
        int deliveryTipAmount = deliveryTip.breakdown().totalTipAmount();

        OrderSchedule schedule = resolveSchedule(shop, shopId, placement);

        int couponDiscountAmount = 0;
        MemberCouponId memberCouponId = null;
        if (placement.memberCouponId() != null) {
            CouponUseResult couponResult = couponIssueService.useCoupon(
                MemberCouponId.of(placement.memberCouponId()), memberId, orderAmountAfterProductDiscount
            );
            couponDiscountAmount = couponResult.couponDiscountAmount();
            memberCouponId = MemberCouponId.of(couponResult.memberCouponId());
        }

        int pointDiscountAmount = 0;
        if (placement.usePoint() > 0) {
            pointDiscountAmount = placement.usePoint();
            pointLedgerService.usePoints(memberId, pointDiscountAmount);
        }

        int totalDiscountAmount = productDiscountAmount + couponDiscountAmount + pointDiscountAmount;
        // 배달팁은 이 도메인에서 유일하게 더해지는 금액 항목이다(나머지는 전부 차감).
        int finalAmount = totalProductAmount - totalDiscountAmount + deliveryTipAmount;

        validateAmounts(placement, totalProductAmount, totalDiscountAmount, productDiscountAmount,
            couponDiscountAmount, pointDiscountAmount, deliveryTipAmount, finalAmount);

        savedOrder.updateAmounts(totalProductAmount, productDiscountAmount, couponDiscountAmount,
            pointDiscountAmount, totalDiscountAmount, deliveryTipAmount, finalAmount,
            deliveryTip.destination(), schedule, memberCouponId, pointDiscountAmount);
        orderRepository.save(savedOrder);

        return savedOrder.getOrderId();
    }

    /**
     * 주문 요청의 상품 라인을 product 컨텍스트의 검증 입력으로 옮겨 담는다.
     *
     * <p>{@code OrderPlacementItem}을 그대로 넘기면 product가 order 내부를 알게 되므로, 검증에 필요한
     * 값(상품 id·수량·선택 옵션)만 product 소유 입력 타입으로 변환한다.
     */
    private static List<OrderLineSelection> toSelections(OrderPlacement placement) {
        List<OrderLineSelection> selections = new ArrayList<>();
        for (OrderPlacementItem item : placement.items()) {
            List<OrderLineOptionSelection> options = new ArrayList<>();
            if (item.selectedOptions() != null) {
                for (OrderPlacementItemOption selected : item.selectedOptions()) {
                    // groupId·optionId가 둘 다 Long이라 자리를 바꿔도 컴파일된다 — 순서를 대조할 것.
                    options.add(OrderLineOptionSelection.of(selected.groupId(), selected.optionId()));
                }
            }
            selections.add(OrderLineSelection.of(item.productId(), item.quantity(), options));
        }
        return selections;
    }

    /**
     * 5단계 — 배달 목적지를 확정하고 배달팁을 산출한다(배달이 아닌 주문은 목적지 없음 · 팁 0원).
     *
     * <p>이 단계를 쿠폰·포인트보다 <b>앞</b>에 두는 이유는 주소 누락·배달불가 지역 실패가 쿠폰이 소모되기
     * 전에 터져야 진단이 단순하기 때문이다(롤백은 되지만 실패 지점이 앞설수록 좋다).
     *
     * <p><b>좌표는 저장된 주소에서만 읽는다.</b> 클라이언트는 {@code deliveryAddressId}만 보내며,
     * 좌표를 요청 본문으로 받으면 가짜 좌표로 거리별 팁을 0원까지 낮출 수 있다. 주소 로드와 소유권
     * 검증은 member 컨텍스트의 {@link MemberDeliveryAddressService}가 담당한다.
     *
     * <p>확정 시각은 <b>주문 접수 시점의 서버 시각</b>이다. 주문서 진입 시점과 시간별 팁 구간이 달라지면
     * 8단계 금액 대조에서 거절되고 프론트가 재견적 후 재시도한다 — "화면에서 본 시점이 아니라 결제하는
     * 시점이 기준"이라는 기존 품절 검사 철학과 같은 선택이며, 조용히 다른 금액을 결제시키지 않는다.
     */
    private DeliveryTipResolution resolveDeliveryTip(
        ShopOrderContextService.OrderableShop shop,
        ShopId shopId,
        MemberId memberId,
        OrderPlacement placement,
        int orderAmountAfterProductDiscount
    ) {
        if (placement.orderMethod() != OrderMethod.DELIVERY) {
            return new DeliveryTipResolution(OrderDeliveryDestination.none(), ShopDeliveryTipBreakdown.none());
        }

        if (placement.deliveryAddressId() == null) {
            throw new BusinessException(ErrorCode.ORDER_DELIVERY_ADDRESS_REQUIRED);
        }

        var address = memberDeliveryAddressService.findOwnedAddress(memberId, placement.deliveryAddressId());

        LocalDateTime orderedAt = LocalDateTime.now();
        ShopDeliveryResolution resolution = shopOrderContextService.resolveDelivery(
            shop,
            shopId,
            ShopOrderContextService.DeliveryDestinationSpec.of(
                address.getAdminDongId(), address.getLatitude(), address.getLongitude()
            ),
            placement.orderMethod(),
            orderAmountAfterProductDiscount,
            orderedAt,
            publicHolidayCalendar.isPublicHoliday(orderedAt.toLocalDate())
        );

        // 인자 순서는 OrderDeliveryDestination의 컴포넌트 선언 순서(알파벳순)와 같아야 한다 —
        // 주소 3종이 전부 String, 좌표 2종이 전부 BigDecimal이라 어긋나도 컴파일된다.
        OrderDeliveryDestination destination = OrderDeliveryDestination.of(
            address.getAdminDongId() == null ? null : address.getAdminDongId().value(),
            address.getDetailAddress(),
            resolution.distanceMeters(),
            address.getLatitude(),
            address.getLongitude(),
            address.getLotAddress(),
            address.getRoadAddress()
        );

        return new DeliveryTipResolution(destination, resolution.tipBreakdown());
    }

    /**
     * 5.5단계 — 수령 예약시간을 확정한다(즉시 주문은 빈 값).
     *
     * <p>이 단계를 배달팁 뒤·쿠폰 앞에 두는 이유는 배달팁과 같다 — 예약 불가 실패가 쿠폰이 소모되기 전에
     * 터져야 진단이 단순하다. <b>금액에는 영향이 없다</b>: 배달팁·최소주문금액·쿠폰/포인트 상한 계산이
     * 그대로이고 {@code finalAmount} 공식도 바뀌지 않는다.
     *
     * <p>배달팁 기준 시각도 <b>수령 시각이 아니라 주문(결제) 시각</b>을 그대로 유지한다 — PDF의 "금액은
     * 결제 시점 기준 확정" 원칙상, 미래의 수령 시각으로 시간별 팁을 매기면 결제 후 팁이 달라지는 것과 같은
     * 효과가 난다.
     *
     * <p>클라이언트가 보낸 시각을 신뢰하지 않고 서버가 슬롯을 재계산해 대조한다 — 배달팁 7항목 금액 대조와
     * 같은 원칙이며, 대조는 shop 컨텍스트가 수행한다.
     */
    private OrderSchedule resolveSchedule(
        ShopOrderContextService.OrderableShop shop,
        ShopId shopId,
        OrderPlacement placement
    ) {
        if (placement.scheduledAt() == null) {
            return OrderSchedule.none();
        }

        ScheduledOrderSlot slot = shopOrderContextService.resolveScheduledSlot(
            shop, shopId, placement.orderMethod(), placement.scheduledAt(), LocalDateTime.now()
        );

        // 두 인자가 같은 LocalDateTime이라 자리를 바꿔도 컴파일된다 — 시작·종료 순서를 대조할 것.
        return OrderSchedule.of(slot.startAt(), slot.endAt());
    }

    /**
     * 검증을 통과한 라인의 옵션들을 저장하고 옵션 추가 금액 합계를 돌려준다.
     *
     * <p>옵션 존재 검증은 이미 product 컨텍스트가 끝냈으므로, 여기서는 <b>주문 당시 값으로 박제</b>된
     * 스냅샷을 저장하기만 한다.
     */
    private int saveSelectedOptions(OrderProduct savedOrderProduct, OrderProductSnapshot snapshot) {
        int totalOptionPrice = 0;
        for (OrderProductOptionSnapshot option : snapshot.options()) {
            orderProductOptionRepository.save(OrderProductOption.of(
                savedOrderProduct.getOrderProductId(),
                option.optionGroupId(),
                option.optionGroupName(),
                option.optionId(),
                option.optionName(),
                option.additionalPrice()
            ));

            totalOptionPrice += option.additionalPrice();
        }
        return totalOptionPrice;
    }

    /**
     * 클라이언트가 계산해 보내온 금액과 서버 계산 금액을 항목별로 대조한다 — 불일치는 위조 시도로 보고 거절한다.
     */
    private void validateAmounts(
        OrderPlacement placement,
        int totalProductAmount,
        int totalDiscountAmount,
        int productDiscountAmount,
        int couponDiscountAmount,
        int pointDiscountAmount,
        int deliveryTipAmount,
        int finalAmount
    ) {
        if (!placement.totalProductAmount().equals(totalProductAmount)) {
            throw new BusinessException(ErrorCode.ORDER_PRODUCT_AMOUNT_MISMATCH,
                ErrorCode.ORDER_PRODUCT_AMOUNT_MISMATCH.getDefaultMessage() + " 요청: " + placement.totalProductAmount() + ", 계산: " + totalProductAmount);
        }
        if (!placement.productDiscountAmount().equals(productDiscountAmount)) {
            throw new BusinessException(ErrorCode.ORDER_PRODUCT_DISCOUNT_AMOUNT_MISMATCH,
                ErrorCode.ORDER_PRODUCT_DISCOUNT_AMOUNT_MISMATCH.getDefaultMessage() + " 요청: " + placement.productDiscountAmount() + ", 계산: " + productDiscountAmount);
        }
        if (!placement.couponDiscountAmount().equals(couponDiscountAmount)) {
            throw new BusinessException(ErrorCode.ORDER_COUPON_DISCOUNT_AMOUNT_MISMATCH,
                ErrorCode.ORDER_COUPON_DISCOUNT_AMOUNT_MISMATCH.getDefaultMessage() + " 요청: " + placement.couponDiscountAmount() + ", 계산: " + couponDiscountAmount);
        }
        if (!placement.usePoint().equals(pointDiscountAmount)) {
            throw new BusinessException(ErrorCode.ORDER_POINT_DISCOUNT_AMOUNT_MISMATCH,
                ErrorCode.ORDER_POINT_DISCOUNT_AMOUNT_MISMATCH.getDefaultMessage() + " 요청: " + placement.usePoint() + ", 계산: " + pointDiscountAmount);
        }
        if (!placement.totalDiscountAmount().equals(totalDiscountAmount)) {
            throw new BusinessException(ErrorCode.ORDER_TOTAL_DISCOUNT_AMOUNT_MISMATCH,
                ErrorCode.ORDER_TOTAL_DISCOUNT_AMOUNT_MISMATCH.getDefaultMessage() + " 요청: " + placement.totalDiscountAmount() + ", 계산: " + totalDiscountAmount);
        }
        if (!placement.deliveryTipAmount().equals(deliveryTipAmount)) {
            throw new BusinessException(ErrorCode.ORDER_DELIVERY_TIP_AMOUNT_MISMATCH,
                ErrorCode.ORDER_DELIVERY_TIP_AMOUNT_MISMATCH.getDefaultMessage() + " 요청: " + placement.deliveryTipAmount() + ", 계산: " + deliveryTipAmount);
        }
        if (!placement.finalAmount().equals(finalAmount)) {
            throw new BusinessException(ErrorCode.ORDER_FINAL_AMOUNT_MISMATCH,
                ErrorCode.ORDER_FINAL_AMOUNT_MISMATCH.getDefaultMessage() + " 요청: " + placement.finalAmount() + ", 계산: " + finalAmount);
        }
    }

    /**
     * 5단계 산출 결과 — 배달 목적지 스냅샷과 배달팁 내역을 함께 나른다.
     *
     * <p>둘은 같은 판정에서 함께 나오고 9단계에서 함께 소비되므로, 둘을 따로 돌려주면 호출부가 두 값을
     * 짝지어 들고 다녀야 한다.
     */
    private record DeliveryTipResolution(
        OrderDeliveryDestination destination,
        ShopDeliveryTipBreakdown breakdown
    ) {
    }

    private String generateOrderNumber() {
        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        return "ORD-" + dateTime + "-" + uuid;
    }
}
