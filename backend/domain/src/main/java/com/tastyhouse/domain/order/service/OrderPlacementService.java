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
            0, 0, 0, 0, 0, 0, 0, 0, OrderDeliveryDestination.none(), OrderSchedule.none(), null, 0, 0
        );
        Order savedOrder = orderRepository.save(order);

        List<OrderProductSnapshot> snapshots = orderProductValidationService.validate(
            toSelections(placement), placement.orderMethod(), LocalDateTime.now());

        int totalProductAmount = 0;
        int productDiscountAmount = 0;
        int cupDepositAmount = 0;

        for (OrderProductSnapshot snapshot : snapshots) {
            OrderProduct orderProduct = OrderProduct.of(
                savedOrder.getOrderId(),
                snapshot.productId(),
                snapshot.name(),
                snapshot.priceName(),
                snapshot.representativeImageFileId(),
                snapshot.quantity(),
                snapshot.originalPrice(),
                snapshot.discountPrice(),
                0, 0, 0
            );
            OrderProduct savedOrderProduct = orderProductRepository.save(orderProduct);

            OrderLineOptionAmounts optionAmounts = saveSelectedOptions(savedOrderProduct, snapshot);
            int totalOptionPrice = optionAmounts.totalOptionPrice();

            int itemTotal = (snapshot.effectivePrice() + totalOptionPrice) * snapshot.quantity();
            int itemDiscount = snapshot.discountPrice() != null
                ? (snapshot.originalPrice() - snapshot.discountPrice()) * snapshot.quantity()
                : 0;

            int itemDeposit = optionAmounts.totalDepositAmount() * snapshot.quantity();

            int itemPersonalCupDiscount = optionAmounts.totalPersonalCupDiscount() * snapshot.quantity();

            savedOrderProduct.updatePrices(totalOptionPrice, itemTotal, itemDeposit);
            orderProductRepository.save(savedOrderProduct);

            totalProductAmount += snapshot.originalPrice() * snapshot.quantity()
                + totalOptionPrice * snapshot.quantity();
            productDiscountAmount += itemDiscount + itemPersonalCupDiscount;
            cupDepositAmount += itemDeposit;
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

        int finalAmount = totalProductAmount - totalDiscountAmount + deliveryTipAmount + cupDepositAmount;

        validateAmounts(placement, totalProductAmount, totalDiscountAmount, productDiscountAmount,
            couponDiscountAmount, pointDiscountAmount, deliveryTipAmount, cupDepositAmount, finalAmount);

        savedOrder.updateAmounts(totalProductAmount, productDiscountAmount, couponDiscountAmount,
            pointDiscountAmount, totalDiscountAmount, deliveryTipAmount, cupDepositAmount, finalAmount,
            deliveryTip.destination(), schedule, memberCouponId, pointDiscountAmount);
        orderRepository.save(savedOrder);

        return savedOrder.getOrderId();
    }

    private static List<OrderLineSelection> toSelections(OrderPlacement placement) {
        List<OrderLineSelection> selections = new ArrayList<>();
        for (OrderPlacementItem item : placement.items()) {
            List<OrderLineOptionSelection> options = new ArrayList<>();
            if (item.selectedOptions() != null) {
                for (OrderPlacementItemOption selected : item.selectedOptions()) {
                    options.add(OrderLineOptionSelection.of(selected.groupId(), selected.optionId()));
                }
            }
            selections.add(OrderLineSelection.of(item.productId(), item.priceId(), item.quantity(), options));
        }
        return selections;
    }

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

        return OrderSchedule.of(slot.startAt(), slot.endAt());
    }

    private OrderLineOptionAmounts saveSelectedOptions(
        OrderProduct savedOrderProduct,
        OrderProductSnapshot snapshot
    ) {
        int totalOptionPrice = 0;
        int totalDepositAmount = 0;
        int totalPersonalCupDiscount = 0;
        for (OrderProductOptionSnapshot option : snapshot.options()) {
            orderProductOptionRepository.save(OrderProductOption.of(
                savedOrderProduct.getOrderProductId(),
                option.optionGroupId(),
                option.optionGroupName(),
                option.optionId(),
                option.optionName(),
                option.additionalPrice(),
                option.optionGroupType(),
                option.cupCount(),
                option.depositAmount()
            ));

            totalOptionPrice += option.additionalPrice();
            totalDepositAmount += option.depositAmount();
            totalPersonalCupDiscount += option.personalCupDiscountAmount();
        }
        return new OrderLineOptionAmounts(totalOptionPrice, totalDepositAmount, totalPersonalCupDiscount);
    }

    private void validateAmounts(
        OrderPlacement placement,
        int totalProductAmount,
        int totalDiscountAmount,
        int productDiscountAmount,
        int couponDiscountAmount,
        int pointDiscountAmount,
        int deliveryTipAmount,
        int cupDepositAmount,
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

        if (!orZero(placement.cupDepositAmount()).equals(cupDepositAmount)) {
            throw new BusinessException(ErrorCode.ORDER_CUP_DEPOSIT_AMOUNT_MISMATCH,
                ErrorCode.ORDER_CUP_DEPOSIT_AMOUNT_MISMATCH.getDefaultMessage()
                    + " 요청: " + placement.cupDepositAmount() + ", 계산: " + cupDepositAmount);
        }
        if (!placement.finalAmount().equals(finalAmount)) {
            throw new BusinessException(ErrorCode.ORDER_FINAL_AMOUNT_MISMATCH,
                ErrorCode.ORDER_FINAL_AMOUNT_MISMATCH.getDefaultMessage() + " 요청: " + placement.finalAmount() + ", 계산: " + finalAmount);
        }
    }

    private static Integer orZero(Integer value) {
        return value != null ? value : 0;
    }

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
