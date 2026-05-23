package com.tastyhouse.webapi.order;

import com.tastyhouse.core.common.PageResult;
import com.tastyhouse.core.domain.coupon.application.CouponCommandService;
import com.tastyhouse.core.domain.coupon.application.dto.command.UseCouponCommand;
import com.tastyhouse.core.domain.coupon.application.dto.result.UseCouponResult;
import com.tastyhouse.core.entity.order.Order;
import com.tastyhouse.core.entity.order.OrderItem;
import com.tastyhouse.core.entity.order.OrderItemOption;
import com.tastyhouse.core.entity.order.OrderStatus;
import com.tastyhouse.core.entity.payment.Payment;
import com.tastyhouse.core.entity.place.Place;
import com.tastyhouse.core.entity.product.Product;
import com.tastyhouse.core.entity.product.ProductOption;
import com.tastyhouse.core.entity.product.ProductOptionGroup;
import com.tastyhouse.core.entity.user.Member;
import com.tastyhouse.core.exception.AccessDeniedException;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.domain.point.application.PointCommandService;
import com.tastyhouse.core.domain.point.application.dto.command.UsePointCommand;
import com.tastyhouse.core.service.OrderCoreService;
import com.tastyhouse.core.service.ProductCoreService;
import com.tastyhouse.core.service.PlaceCoreService;
import com.tastyhouse.core.service.MemberCoreService;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.webapi.member.response.OrderListItemResponse;
import com.tastyhouse.webapi.order.request.OrderCreateRequest;
import com.tastyhouse.webapi.order.request.OrderItemOptionRequest;
import com.tastyhouse.webapi.order.request.OrderItemRequest;
import com.tastyhouse.webapi.order.response.OrderItemOptionResponse;
import com.tastyhouse.webapi.order.response.OrderItemResponse;
import com.tastyhouse.webapi.order.response.OrderResponse;
import com.tastyhouse.webapi.order.response.PaymentSummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderCoreService orderCoreService;
    private final PointCommandService pointCommandService;
    private final CouponCommandService couponCommandService;
    private final ProductCoreService productCoreService;
    private final PlaceCoreService placeCoreService;
    private final MemberCoreService memberCoreService;
    private final FileService fileService;

    @Transactional
    public OrderResponse createOrder(Long memberId, OrderCreateRequest request) {
        Place place = placeCoreService.findPlaceById(request.placeId());

        Member member = memberCoreService.getById(memberId);

        int totalProductAmount = 0;
        int productDiscountAmount = 0;

        Order order =
            Order.of(
                memberId,
                request.placeId(),
                generateOrderNumber(),
                OrderStatus.PENDING,
                member.getFullName(),
                member.getPhoneNumber().getValue(),
                member.getUsername(),
                0,
                0,
                0,
                0,
                0,
                0,
                null,
                0,
                0
            );

        Order savedOrder = orderCoreService.saveOrder(order);

        for (OrderItemRequest itemRequest : request.orderItems()) {
            Product product = productCoreService.findProductById(itemRequest.productId()).orElseThrow(() -> new EntityNotFoundException(ErrorCode.ORDER_PRODUCT_NOT_FOUND, ErrorCode.ORDER_PRODUCT_NOT_FOUND.getDefaultMessage() + ": " + itemRequest.productId()));

            if (product.getIsSoldOut()) {
                throw new BusinessException(ErrorCode.ORDER_PRODUCT_SOLD_OUT, ErrorCode.ORDER_PRODUCT_SOLD_OUT.getDefaultMessage() + ": " + product.getName());
            }

            String productImageUrl = productCoreService.getFirstImageFilePath(product.getId());

            int unitPrice = product.getOriginalPrice();
            Integer discountPrice = product.getDiscountPrice();
            int optionTotalPrice = 0;

            OrderItem orderItem =
                OrderItem.of(
                    savedOrder.getId(),
                    product.getId(),
                    product.getName(),
                    fileService.getUrlByPath(productImageUrl),
                    itemRequest.quantity(),
                    unitPrice,
                    discountPrice,
                    0,
                    0
                );

            OrderItem savedOrderItem = orderCoreService.saveOrderItem(orderItem);

            if (itemRequest.selectedOptions() != null) {
                for (OrderItemOptionRequest optionRequest : itemRequest.selectedOptions()) {
                    ProductOptionGroup optionGroup = productCoreService.findProductOptionGroupById(optionRequest.groupId()).orElseThrow(() -> new EntityNotFoundException(ErrorCode.ORDER_OPTION_GROUP_NOT_FOUND));

                    ProductOption option = productCoreService.findProductOptionById(optionRequest.optionId()).orElseThrow(() -> new EntityNotFoundException(ErrorCode.ORDER_OPTION_NOT_FOUND));

                    orderCoreService.saveOrderItemOption(
                        OrderItemOption.of(
                            savedOrderItem.getId(),
                            optionGroup.getId(),
                            optionGroup.getName(),
                            option.getId(),
                            option.getName(),
                            option.getAdditionalPrice()
                        )
                    );

                    optionTotalPrice += option.getAdditionalPrice();
                }
            }

            int effectivePrice = discountPrice != null ? discountPrice : unitPrice;
            int itemTotal = (effectivePrice + optionTotalPrice) * itemRequest.quantity();
            int itemDiscount = discountPrice != null ? (unitPrice - discountPrice) * itemRequest.quantity() : 0;

            savedOrderItem.updatePrices(optionTotalPrice, itemTotal);

            totalProductAmount += unitPrice * itemRequest.quantity() + optionTotalPrice * itemRequest.quantity();
            productDiscountAmount += itemDiscount;
        }

        int couponDiscountAmount = 0;
        Long memberCouponId = null;
        if (request.memberCouponId() != null) {
            int orderAmountAfterProductDiscount = totalProductAmount - productDiscountAmount;
            UseCouponResult couponResult = couponCommandService.useCoupon(
                new UseCouponCommand(request.memberCouponId(), memberId, orderAmountAfterProductDiscount)
            );
            couponDiscountAmount = couponResult.couponDiscountAmount();
            memberCouponId = couponResult.memberCouponId();
        }

        int pointDiscountAmount = 0;
        if (request.usePoint() > 0) {
            pointDiscountAmount = request.usePoint();
            pointCommandService.usePoints(new UsePointCommand(memberId, pointDiscountAmount));
        }

        int totalDiscountAmount = productDiscountAmount + couponDiscountAmount + pointDiscountAmount;
        int finalAmount = totalProductAmount - totalDiscountAmount;

        validateOrderAmounts(request, totalProductAmount, totalDiscountAmount, productDiscountAmount, couponDiscountAmount, pointDiscountAmount, finalAmount);

        savedOrder.updateAmounts(totalProductAmount, productDiscountAmount, couponDiscountAmount, pointDiscountAmount, totalDiscountAmount, finalAmount, memberCouponId, pointDiscountAmount);

        return buildOrderResponse(savedOrder, place, memberId);
    }

    private void validateOrderAmounts(OrderCreateRequest request, int totalProductAmount, int totalDiscountAmount, int productDiscountAmount, int couponDiscountAmount, int pointDiscountAmount, int finalAmount) {
        if (!request.totalProductAmount().equals(totalProductAmount)) {
            throw new BusinessException(ErrorCode.ORDER_PRODUCT_AMOUNT_MISMATCH, ErrorCode.ORDER_PRODUCT_AMOUNT_MISMATCH.getDefaultMessage() + " 요청: " + request.totalProductAmount() + ", 계산: " + totalProductAmount);
        }
        if (!request.productDiscountAmount().equals(productDiscountAmount)) {
            throw new BusinessException(ErrorCode.ORDER_PRODUCT_DISCOUNT_AMOUNT_MISMATCH, ErrorCode.ORDER_PRODUCT_DISCOUNT_AMOUNT_MISMATCH.getDefaultMessage() + " 요청: " + request.productDiscountAmount() + ", 계산: " + productDiscountAmount);
        }
        if (!request.couponDiscountAmount().equals(couponDiscountAmount)) {
            throw new BusinessException(ErrorCode.ORDER_COUPON_DISCOUNT_AMOUNT_MISMATCH, ErrorCode.ORDER_COUPON_DISCOUNT_AMOUNT_MISMATCH.getDefaultMessage() + " 요청: " + request.couponDiscountAmount() + ", 계산: " + couponDiscountAmount);
        }
        if (!request.usePoint().equals(pointDiscountAmount)) {
            throw new BusinessException(ErrorCode.ORDER_POINT_DISCOUNT_AMOUNT_MISMATCH, ErrorCode.ORDER_POINT_DISCOUNT_AMOUNT_MISMATCH.getDefaultMessage() + " 요청: " + request.usePoint() + ", 계산: " + pointDiscountAmount);
        }
        if (!request.totalDiscountAmount().equals(totalDiscountAmount)) {
            throw new BusinessException(ErrorCode.ORDER_TOTAL_DISCOUNT_AMOUNT_MISMATCH, ErrorCode.ORDER_TOTAL_DISCOUNT_AMOUNT_MISMATCH.getDefaultMessage() + " 요청: " + request.totalDiscountAmount() + ", 계산: " + totalDiscountAmount);
        }
        if (!request.finalAmount().equals(finalAmount)) {
            throw new BusinessException(ErrorCode.ORDER_FINAL_AMOUNT_MISMATCH, ErrorCode.ORDER_FINAL_AMOUNT_MISMATCH.getDefaultMessage() + " 요청: " + request.finalAmount() + ", 계산: " + finalAmount);
        }
    }

    @Transactional(readOnly = true)
    public PageResult<OrderListItemResponse> getOrderList(Long memberId, int page, int size) {
        return PageResult.from(orderCoreService.findOrderListByMemberId(memberId, page, size))
            .map(dto -> OrderListItemResponse.from(
                dto.id(),
                dto.placeName(),
                fileService.getUrlByPath(dto.placeThumbnailImageFilePath()),
                dto.firstProductName(),
                dto.totalItemCount(),
                dto.amount(),
                dto.paymentStatus(),
                dto.paymentDate()
            ));
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderDetail(Long memberId, Long orderId) {
        Order order = orderCoreService.findOrderById(orderId).orElseThrow(() -> new EntityNotFoundException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getMemberId().equals(memberId)) {
            throw new AccessDeniedException(ErrorCode.ORDER_ACCESS_DENIED);
        }

        Place place = placeCoreService.findPlaceById(order.getPlaceId());
        return buildOrderResponse(order, place, memberId);
    }

    private String generateOrderNumber() {
        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        return "ORD-" + dateTime + "-" + uuid;
    }

    private OrderResponse buildOrderResponse(Order order, Place place, Long memberId) {
        List<OrderItem> items = orderCoreService.findOrderItemsByOrderId(order.getId());

        List<OrderItemResponse> itemResponses = items.stream().map(item -> {
            List<OrderItemOption> options = orderCoreService.findOrderItemOptionsByOrderItemId(item.getId());

            List<OrderItemOptionResponse> optionResponses =
                options.stream()
                    .map(opt -> OrderItemOptionResponse.from(
                        opt.getId(),
                        opt.getOptionGroupName(),
                        opt.getOptionName(),
                        opt.getAdditionalPrice()))
                    .toList();

            boolean isReviewed = orderCoreService.existsReviewByOrderIdAndProductIdAndMemberId(order.getId(), item.getProductId(), memberId);

            return OrderItemResponse.from(
                item.getId(),
                item.getProductId(),
                item.getProductName(),
                fileService.getUrlByPath(item.getProductImageUrl()),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getDiscountPrice(),
                item.getOptionTotalPrice(),
                item.getTotalPrice(),
                isReviewed,
                optionResponses
            );
        }).toList();

        PaymentSummaryResponse paymentSummary = null;
        Payment payment = orderCoreService.findPaymentByOrderId(order.getId()).orElse(null);
        if (payment != null) {
            paymentSummary = PaymentSummaryResponse.from(
                payment.getId(),
                payment.getPaymentMethod(),
                payment.getPaymentStatus(),
                payment.getAmount(),
                payment.getCardCompany(),
                payment.getCardNumber(),
                payment.getApprovedAt(),
                payment.getReceiptUrl()
            );
        }

        return OrderResponse.from(
            order.getId(),
            order.getOrderNumber(),
            payment != null ? payment.getPaymentStatus() : null,
            place != null ? place.getName() : null,
            place != null ? place.getPhoneNumber() : null,
            order.getOrdererName(),
            order.getOrdererPhone(),
            order.getOrdererEmail(),
            order.getTotalProductAmount(),
            order.getProductDiscountAmount(),
            order.getCouponDiscountAmount(),
            order.getPointDiscountAmount(),
            order.getTotalDiscountAmount(),
            order.getFinalAmount(),
            order.getUsedPoint(),
            order.getEarnedPoint(),
            itemResponses,
            paymentSummary, payment != null ? payment.getApprovedAt() : null,
            order.getCreatedAt()
        );
    }
}
