package com.tastyhouse.domain.order.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import com.tastyhouse.domain.coupon.service.CouponIssueService;
import com.tastyhouse.domain.coupon.service.CouponUseResult;
import com.tastyhouse.domain.coupon.vo.MemberCouponId;
import com.tastyhouse.domain.holiday.service.PublicHolidayCalendar;
import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.domain.member.model.MemberDeliveryAddress;
import com.tastyhouse.domain.member.repository.MemberDeliveryAddressRepository;
import com.tastyhouse.domain.member.repository.MemberRepository;
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
import com.tastyhouse.domain.point.service.PointLedgerService;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.repository.ProductImageRepository;
import com.tastyhouse.domain.product.repository.ProductOptionGroupRepository;
import com.tastyhouse.domain.product.repository.ProductOptionRepository;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.product.vo.ProductOptionId;
import com.tastyhouse.domain.shop.model.OrderMethod;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.repository.ShopDeliveryAreaRepository;
import com.tastyhouse.domain.shop.repository.ShopDeliveryTipRepository;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.service.ShopDeliveryTipBreakdown;
import com.tastyhouse.domain.shop.service.ShopDeliveryTipCalculator;
import com.tastyhouse.domain.shop.service.ShopDeliveryTipContext;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shared.geo.GeoDistance;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

/**
 * 주문 접수 불변식(도메인 서비스).
 *
 * <p>주문 한 건의 접수는 {@code Order} 헤더 · 상품 라인({@code OrderProduct}) · 라인 옵션
 * ({@code OrderProductOption}) 세 애그리거트를 한 트랜잭션에서 함께 만들고, 그 과정에서 계산한 금액을
 * 헤더에 되반영해야 하는 원자 연산이다. 세 애그리거트 중 하나라도 빠지면 주문이 반쪽으로 저장되고,
 * 금액 되반영이 빠지면 결제 금액이 0원인 주문이 남는다. 여기에 판매중지 검증 · 쿠폰 사용
 * ({@link CouponIssueService}) · 포인트 차감({@link PointLedgerService})까지 같은 트랜잭션에 묶이는
 * 크로스 애그리거트 불변식 오케스트레이션(분류 C)이므로 도메인 계층에 둔다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code DomainServiceConfig}가 담당한다. 트랜잭션 경계는 이 서비스를 호출하는
 * 소비 모듈의 command 서비스(web-api {@code OrderCommandService})가 선언한다.
 *
 * <p>주문 접수는 도메인 이벤트를 발행하지 않는다 — 과거 {@code OrderCreatedEvent}를 발행했으나 수신
 * 리스너가 없는 no-op이어서 P9(도메인 이벤트 정비)에서 제거했다. 접수 이후 비동기 후처리(알림·집계)가
 * 필요해지면 이 메서드 말미에 발행을 다시 추가하면 된다. 상태 전이({@code confirm}/{@code cancel})
 * 이벤트도 소비 수요가 생길 때 {@code OrderTransitionService}의 전이 지점에 추가한다(현재는 YAGNI).
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
    private final ShopRepository shopRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final ProductOptionGroupRepository productOptionGroupRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductImageRepository productImageRepository;
    private final CouponIssueService couponIssueService;
    private final PointLedgerService pointLedgerService;
    private final ShopDeliveryTipRepository shopDeliveryTipRepository;
    private final ShopDeliveryAreaRepository shopDeliveryAreaRepository;
    private final MemberDeliveryAddressRepository memberDeliveryAddressRepository;
    private final ShopDeliveryTipCalculator shopDeliveryTipCalculator;
    private final PublicHolidayCalendar publicHolidayCalendar;

    /**
     * @param orderRepository                 주문 헤더 저장·재저장(금액 확정 반영)
     * @param orderProductRepository          상품 라인 저장·가격 갱신
     * @param orderProductOptionRepository    라인 옵션 저장
     * @param shopRepository                  가게 존재 확인과 최소주문금액·좌표 조회
     * @param memberRepository                주문자 정보(이름·연락처) 조회
     * @param productRepository               상품 존재·판매중지·가격 조회
     * @param productOptionGroupRepository    선택 옵션의 그룹 존재 확인
     * @param productOptionRepository         선택 옵션 존재·추가금 조회
     * @param productImageRepository          상품 대표 이미지 경로 스냅샷
     * @param couponIssueService              쿠폰 사용 처리(할인액 산출·상태 전이)
     * @param pointLedgerService              포인트 차감 원장 기록
     * @param shopDeliveryTipRepository       배달팁 설정 5종 조회(계산기 입력 구성)
     * @param shopDeliveryAreaRepository      배달가능지역 등록 여부·포함 여부 판정
     * @param memberDeliveryAddressRepository 배달 주소 로드(좌표는 여기서만 읽는다 — 위조 방지)
     * @param shopDeliveryTipCalculator       배달팁 산출(리포지토리 없는 순수 계산기)
     * @param publicHolidayCalendar           접수 시각의 공휴일 여부 판정
     */
    public OrderPlacementService(
        OrderRepository orderRepository,
        OrderProductRepository orderProductRepository,
        OrderProductOptionRepository orderProductOptionRepository,
        ShopRepository shopRepository,
        MemberRepository memberRepository,
        ProductRepository productRepository,
        ProductOptionGroupRepository productOptionGroupRepository,
        ProductOptionRepository productOptionRepository,
        ProductImageRepository productImageRepository,
        CouponIssueService couponIssueService,
        PointLedgerService pointLedgerService,
        ShopDeliveryTipRepository shopDeliveryTipRepository,
        ShopDeliveryAreaRepository shopDeliveryAreaRepository,
        MemberDeliveryAddressRepository memberDeliveryAddressRepository,
        ShopDeliveryTipCalculator shopDeliveryTipCalculator,
        PublicHolidayCalendar publicHolidayCalendar
    ) {
        this.orderRepository = orderRepository;
        this.orderProductRepository = orderProductRepository;
        this.orderProductOptionRepository = orderProductOptionRepository;
        this.shopRepository = shopRepository;
        this.memberRepository = memberRepository;
        this.productRepository = productRepository;
        this.productOptionGroupRepository = productOptionGroupRepository;
        this.productOptionRepository = productOptionRepository;
        this.productImageRepository = productImageRepository;
        this.couponIssueService = couponIssueService;
        this.pointLedgerService = pointLedgerService;
        this.shopDeliveryTipRepository = shopDeliveryTipRepository;
        this.shopDeliveryAreaRepository = shopDeliveryAreaRepository;
        this.memberDeliveryAddressRepository = memberDeliveryAddressRepository;
        this.shopDeliveryTipCalculator = shopDeliveryTipCalculator;
        this.publicHolidayCalendar = publicHolidayCalendar;
    }

    /**
     * 주문을 접수한다(9단계) — 가게·회원 존재 확인 → 주문 헤더 생성 → 상품 라인·옵션 생성과 금액 집계 →
     * 가게 최소주문금액 검증 → <b>배달 목적지 확정과 배달팁 산출</b> → 쿠폰·포인트 사용 → 금액 대조 검증
     * → 헤더 금액 반영.
     *
     * <p>가게 최소주문금액 검증은 상품 할인까지 반영한 금액을 기준으로 쿠폰·포인트 사용 <b>전에</b> 수행한다
     * (판정 기준과 면제 조건은 {@code Shop#validateMinOrderAmount} 참고). 이 검증은 쿠폰 자체의 최소주문금액
     * 검증({@code Coupon#validateMinOrderAmount})과 별개이며, <b>배달팁은 그 판정 기준에 포함되지 않는다</b> —
     * 포함하면 팁이 비싼 가게일수록 최소주문 문턱이 낮아지는 역설이 생긴다.
     *
     * <p>쿠폰·포인트의 할인 기준과 상한에도 배달팁은 <b>포함되지 않는다</b> — 팁만 남기고 상품값을 0으로
     * 만드는 조합을 차단하고, 팁은 프로모션 재원과 성격이 다르기 때문이다.
     *
     * @return 생성된 주문 식별자
     */
    public OrderId place(MemberId memberId, OrderPlacement placement) {
        ShopId shopId = ShopId.of(placement.shopId());
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        Order order = Order.of(
            memberId,
            shopId,
            generateOrderNumber(),
            placement.orderMethod(),
            OrderStatus.PENDING,
            member.getFullName(),
            member.getPhoneNumber().value(),
            member.getUsername(),
            0, 0, 0, 0, 0, 0, 0, OrderDeliveryDestination.none(), null, 0, 0
        );
        Order savedOrder = orderRepository.save(order);

        int totalProductAmount = 0;
        int productDiscountAmount = 0;

        for (OrderPlacementItem item : placement.items()) {
            Product product = productRepository.findById(ProductId.of(item.productId()))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_PRODUCT_NOT_FOUND,
                    ErrorCode.ORDER_PRODUCT_NOT_FOUND.getDefaultMessage() + ": " + item.productId()));

            if (product.isSoldOut()) {
                throw new BusinessException(ErrorCode.ORDER_PRODUCT_SOLD_OUT,
                    ErrorCode.ORDER_PRODUCT_SOLD_OUT.getDefaultMessage() + ": " + product.getName());
            }

            String productImageFilePath = productImageRepository.findRepresentativeImageFilePath(product.getProductId());
            int originalPrice = product.getOriginalPrice();
            Integer discountPrice = product.getDiscountPrice();

            OrderProduct orderProduct = OrderProduct.of(
                savedOrder.getOrderId(),
                product.getProductId(),
                product.getName(),
                productImageFilePath,
                item.quantity(),
                originalPrice,
                discountPrice,
                0, 0
            );
            OrderProduct savedOrderProduct = orderProductRepository.save(orderProduct);

            int totalOptionPrice = saveSelectedOptions(savedOrderProduct, item);

            int effectivePrice = discountPrice != null ? discountPrice : originalPrice;
            int itemTotal = (effectivePrice + totalOptionPrice) * item.quantity();
            int itemDiscount = discountPrice != null ? (originalPrice - discountPrice) * item.quantity() : 0;

            savedOrderProduct.updatePrices(totalOptionPrice, itemTotal);
            orderProductRepository.save(savedOrderProduct);

            totalProductAmount += originalPrice * item.quantity() + totalOptionPrice * item.quantity();
            productDiscountAmount += itemDiscount;
        }

        int orderAmountAfterProductDiscount = totalProductAmount - productDiscountAmount;
        shop.validateMinOrderAmount(placement.orderMethod(), orderAmountAfterProductDiscount);

        DeliveryTipResolution deliveryTip = resolveDeliveryTip(shop, shopId, memberId, placement,
            orderAmountAfterProductDiscount);
        int deliveryTipAmount = deliveryTip.breakdown().totalTipAmount();

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
            deliveryTip.destination(), memberCouponId, pointDiscountAmount);
        orderRepository.save(savedOrder);

        return savedOrder.getOrderId();
    }

    /**
     * 5단계 — 배달 목적지를 확정하고 배달팁을 산출한다(배달이 아닌 주문은 목적지 없음 · 팁 0원).
     *
     * <p>이 단계를 쿠폰·포인트보다 <b>앞</b>에 두는 이유는 주소 누락·배달불가 지역 실패가 쿠폰이 소모되기
     * 전에 터져야 진단이 단순하기 때문이다(롤백은 되지만 실패 지점이 앞설수록 좋다).
     *
     * <p><b>좌표는 저장된 주소에서만 읽는다.</b> 클라이언트는 {@code deliveryAddressId}만 보내며,
     * 좌표를 요청 본문으로 받으면 가짜 좌표로 거리별 팁을 0원까지 낮출 수 있다.
     *
     * <p>확정 시각은 <b>주문 접수 시점의 서버 시각</b>이다. 주문서 진입 시점과 시간별 팁 구간이 달라지면
     * 8단계 금액 대조에서 거절되고 프론트가 재견적 후 재시도한다 — "화면에서 본 시점이 아니라 결제하는
     * 시점이 기준"이라는 기존 품절 검사 철학과 같은 선택이며, 조용히 다른 금액을 결제시키지 않는다.
     */
    private DeliveryTipResolution resolveDeliveryTip(
        Shop shop,
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

        MemberDeliveryAddress address = memberDeliveryAddressRepository.findById(placement.deliveryAddressId())
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.MEMBER_DELIVERY_ADDRESS_NOT_FOUND));
        if (!address.isOwnedBy(memberId)) {
            throw new BusinessException(ErrorCode.MEMBER_DELIVERY_ADDRESS_ACCESS_DENIED);
        }

        validateDeliveryArea(shopId, address);

        double meters = GeoDistance.distanceMeters(
            shop.getLatitude(), shop.getLongitude(), address.getLatitude(), address.getLongitude()
        );
        LocalDateTime orderedAt = LocalDateTime.now();

        ShopDeliveryTipBreakdown breakdown = shopDeliveryTipCalculator.calculate(ShopDeliveryTipContext.of(
            placement.orderMethod(),
            orderAmountAfterProductDiscount,
            meters,
            address.getAdminDongId(),
            orderedAt,
            publicHolidayCalendar.isPublicHoliday(orderedAt.toLocalDate()),
            shopDeliveryTipRepository.findSettingByShopId(shopId).orElse(null),
            shopDeliveryTipRepository.findTiersByShopId(shopId),
            shopDeliveryTipRepository.findRegionTipsByShopId(shopId),
            shopDeliveryTipRepository.findScheduleTipsByShopId(shopId),
            shopDeliveryTipRepository.findHolidayTipByShopId(shopId).orElse(null)
        ));

        return new DeliveryTipResolution(
            OrderDeliveryDestination.of(address, (int) Math.round(meters)),
            breakdown
        );
    }

    /**
     * 배달지가 가게의 배달가능지역에 드는지 검증한다.
     *
     * <p><b>배달가능지역을 하나도 등록하지 않은 가게는 검사를 생략한다.</b> 기존 데이터의 모든 가게가
     * 0건이므로, 항상 거절하면 배포 즉시 전 가게의 배달 주문이 막힌다. "정보를 안 넣은 것을 닫힌 것으로
     * 보지 않는다"는 이 도메인의 기존 원칙(영업시간 미입력을 준비중으로 오판하지 않는 것)과도 일치한다.
     */
    private void validateDeliveryArea(ShopId shopId, MemberDeliveryAddress address) {
        if (shopDeliveryAreaRepository.countByShopId(shopId) == 0) {
            return;
        }
        if (address.getAdminDongId() == null
            || !shopDeliveryAreaRepository.existsByShopIdAndAdminDongId(shopId, address.getAdminDongId())) {
            throw new BusinessException(ErrorCode.ORDER_DELIVERY_AREA_NOT_COVERED);
        }
    }

    /**
     * 상품 라인에서 선택된 옵션들을 저장하고 옵션 추가 금액 합계를 돌려준다.
     */
    private int saveSelectedOptions(OrderProduct savedOrderProduct, OrderPlacementItem item) {
        if (item.selectedOptions() == null) {
            return 0;
        }

        int totalOptionPrice = 0;
        for (OrderPlacementItemOption selectedOption : item.selectedOptions()) {
            ProductOptionGroup optionGroup = productOptionGroupRepository
                .findById(ProductOptionGroupId.of(selectedOption.groupId()))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_OPTION_GROUP_NOT_FOUND));

            ProductOption option = productOptionRepository
                .findById(ProductOptionId.of(selectedOption.optionId()))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_OPTION_NOT_FOUND));

            orderProductOptionRepository.save(OrderProductOption.of(
                savedOrderProduct.getOrderProductId(),
                optionGroup.getProductOptionGroupId(),
                optionGroup.getName(),
                option.getProductOptionId(),
                option.getName(),
                option.getAdditionalPrice()
            ));

            totalOptionPrice += option.getAdditionalPrice();
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
     * <p>둘은 같은 조회(주소 로드 + 거리 산출)에서 함께 나오고 9단계에서 함께 소비되므로, 둘을 따로
     * 돌려주면 호출부가 두 값을 짝지어 들고 다녀야 한다.
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
