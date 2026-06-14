package com.tastyhouse.core.domain.order.application;

import com.tastyhouse.core.domain.order.domain.event.OrderCreatedEvent;
import com.tastyhouse.core.domain.coupon.application.CouponCommandService;
import com.tastyhouse.core.domain.coupon.application.dto.command.UseCouponCommand;
import com.tastyhouse.core.domain.coupon.application.dto.result.UseCouponResult;
import com.tastyhouse.core.domain.member.application.MemberQueryService;
import com.tastyhouse.core.domain.member.domain.model.Member;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.order.application.dto.command.CreateOrderCommand;
import com.tastyhouse.core.domain.order.application.dto.command.CreateOrderProductOptionCommand;
import com.tastyhouse.core.domain.order.application.dto.result.OrderResult;
import com.tastyhouse.core.domain.order.domain.model.Order;
import com.tastyhouse.core.domain.order.domain.model.OrderProduct;
import com.tastyhouse.core.domain.order.domain.model.OrderProductOption;
import com.tastyhouse.core.domain.order.domain.model.OrderStatus;
import com.tastyhouse.core.domain.order.domain.repository.OrderProductOptionRepository;
import com.tastyhouse.core.domain.order.domain.repository.OrderProductRepository;
import com.tastyhouse.core.domain.order.domain.repository.OrderRepository;
import com.tastyhouse.core.domain.shop.application.ShopQueryService;
import com.tastyhouse.core.domain.shop.domain.model.Shop;
import com.tastyhouse.core.domain.point.application.PointCommandService;
import com.tastyhouse.core.domain.point.application.dto.command.UsePointCommand;
import com.tastyhouse.core.domain.product.application.ProductQueryService;
import com.tastyhouse.core.domain.product.domain.model.Product;
import com.tastyhouse.core.domain.product.domain.model.ProductOption;
import com.tastyhouse.core.domain.product.domain.model.ProductOptionGroup;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCommandService {

    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final OrderProductOptionRepository orderProductOptionRepository;
    private final ShopQueryService shopQueryService;
    private final MemberQueryService memberQueryService;
    private final ProductQueryService productQueryService;
    private final CouponCommandService couponCommandService;
    private final PointCommandService pointCommandService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public OrderResult createOrder(Long memberId, CreateOrderCommand command) {
        Shop shop = shopQueryService.findShopById(command.shopId());
        Member member = memberQueryService.getById(new MemberId(memberId));

        Order order = Order.of(
            memberId,
            command.shopId(),
            generateOrderNumber(),
            command.orderMethod(),
            OrderStatus.PENDING,
            member.getFullName(),
            member.getPhoneNumber().getValue(),
            member.getUsername(),
            0, 0, 0, 0, 0, 0, null, 0, 0
        );
        Order savedOrder = orderRepository.save(order);

        int totalProductAmount = 0;
        int productDiscountAmount = 0;

        for (var itemCommand : command.orderProducts()) {
            Product product = productQueryService.findProductById(itemCommand.productId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ORDER_PRODUCT_NOT_FOUND,
                    ErrorCode.ORDER_PRODUCT_NOT_FOUND.getDefaultMessage() + ": " + itemCommand.productId()));

            if (product.getIsSoldOut()) {
                throw new BusinessException(ErrorCode.ORDER_PRODUCT_SOLD_OUT,
                    ErrorCode.ORDER_PRODUCT_SOLD_OUT.getDefaultMessage() + ": " + product.getName());
            }

            String productImageFilePath = productQueryService.getFirstImageFilePath(product.getId());
            int originalPrice = product.getOriginalPrice();
            Integer discountPrice = product.getDiscountPrice();
            int totalOptionPrice = 0;

            OrderProduct orderProduct = OrderProduct.of(
                savedOrder.getId(),
                product.getId(),
                product.getName(),
                productImageFilePath,
                itemCommand.quantity(),
                originalPrice,
                discountPrice,
                0, 0
            );
            OrderProduct savedOrderProduct = orderProductRepository.save(orderProduct);

            if (itemCommand.selectedOptions() != null) {
                for (CreateOrderProductOptionCommand optionCommand : itemCommand.selectedOptions()) {
                    ProductOptionGroup optionGroup = productQueryService
                        .findProductOptionGroupById(optionCommand.groupId())
                        .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ORDER_OPTION_GROUP_NOT_FOUND));

                    ProductOption option = productQueryService
                        .findProductOptionById(optionCommand.optionId())
                        .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ORDER_OPTION_NOT_FOUND));

                    orderProductOptionRepository.save(OrderProductOption.of(
                        savedOrderProduct.getId(),
                        optionGroup.getId(),
                        optionGroup.getName(),
                        option.getId(),
                        option.getName(),
                        option.getAdditionalPrice()
                    ));

                    totalOptionPrice += option.getAdditionalPrice();
                }
            }

            int effectivePrice = discountPrice != null ? discountPrice : originalPrice;
            int itemTotal = (effectivePrice + totalOptionPrice) * itemCommand.quantity();
            int itemDiscount = discountPrice != null ? (originalPrice - discountPrice) * itemCommand.quantity() : 0;

            savedOrderProduct.updatePrices(totalOptionPrice, itemTotal);

            totalProductAmount += originalPrice * itemCommand.quantity() + totalOptionPrice * itemCommand.quantity();
            productDiscountAmount += itemDiscount;
        }

        int couponDiscountAmount = 0;
        Long memberCouponId = null;
        if (command.memberCouponId() != null) {
            int orderAmountAfterProductDiscount = totalProductAmount - productDiscountAmount;
            UseCouponResult couponResult = couponCommandService.useCoupon(
                new UseCouponCommand(command.memberCouponId(), memberId, orderAmountAfterProductDiscount)
            );
            couponDiscountAmount = couponResult.couponDiscountAmount();
            memberCouponId = couponResult.memberCouponId();
        }

        int pointDiscountAmount = 0;
        if (command.usePoint() > 0) {
            pointDiscountAmount = command.usePoint();
            pointCommandService.usePoints(new UsePointCommand(memberId, pointDiscountAmount));
        }

        int totalDiscountAmount = productDiscountAmount + couponDiscountAmount + pointDiscountAmount;
        int finalAmount = totalProductAmount - totalDiscountAmount;

        validateOrderAmounts(command, totalProductAmount, totalDiscountAmount, productDiscountAmount, couponDiscountAmount, pointDiscountAmount, finalAmount);

        savedOrder.updateAmounts(totalProductAmount, productDiscountAmount, couponDiscountAmount, pointDiscountAmount, totalDiscountAmount, finalAmount, memberCouponId, pointDiscountAmount);

        List<OrderProduct> items = orderProductRepository.findByOrderId(savedOrder.getId());
        List<com.tastyhouse.core.domain.order.application.dto.result.OrderProductResult> itemResults = buildOrderProductResults(items);

        eventPublisher.publishEvent(new OrderCreatedEvent(
            savedOrder.getId(),
            memberId,
            savedOrder.getShopId(),
            savedOrder.getFinalAmount(),
            savedOrder.getCreatedAt()
        ));

        return OrderResult.from(
            savedOrder,
            shop != null ? shop.getName() : null,
            shop != null ? shop.getPhoneNumber() : null,
            itemResults,
            null
        );
    }

    private List<com.tastyhouse.core.domain.order.application.dto.result.OrderProductResult> buildOrderProductResults(List<OrderProduct> items) {
        return items.stream()
            .map(item -> {
                List<OrderProductOption> options = orderProductOptionRepository.findByOrderProductId(item.getId());
                List<com.tastyhouse.core.domain.order.application.dto.result.OrderProductOptionResult> optionResults =
                    options.stream()
                        .map(com.tastyhouse.core.domain.order.application.dto.result.OrderProductOptionResult::from)
                        .toList();
                return new com.tastyhouse.core.domain.order.application.dto.result.OrderProductResult(
                    item.getId(),
                    item.getProductId(),
                    item.getName(),
                    item.getImageUrl(),
                    item.getQuantity(),
                    item.getOriginalPrice(),
                    item.getDiscountPrice(),
                    item.getTotalOptionPrice(),
                    item.getTotalPrice(),
                    optionResults
                );
            })
            .toList();
    }

    private void validateOrderAmounts(CreateOrderCommand command, int totalProductAmount, int totalDiscountAmount,
                                      int productDiscountAmount, int couponDiscountAmount,
                                      int pointDiscountAmount, int finalAmount) {
        if (!command.totalProductAmount().equals(totalProductAmount)) {
            throw new BusinessException(ErrorCode.ORDER_PRODUCT_AMOUNT_MISMATCH,
                ErrorCode.ORDER_PRODUCT_AMOUNT_MISMATCH.getDefaultMessage() + " 요청: " + command.totalProductAmount() + ", 계산: " + totalProductAmount);
        }
        if (!command.productDiscountAmount().equals(productDiscountAmount)) {
            throw new BusinessException(ErrorCode.ORDER_PRODUCT_DISCOUNT_AMOUNT_MISMATCH,
                ErrorCode.ORDER_PRODUCT_DISCOUNT_AMOUNT_MISMATCH.getDefaultMessage() + " 요청: " + command.productDiscountAmount() + ", 계산: " + productDiscountAmount);
        }
        if (!command.couponDiscountAmount().equals(couponDiscountAmount)) {
            throw new BusinessException(ErrorCode.ORDER_COUPON_DISCOUNT_AMOUNT_MISMATCH,
                ErrorCode.ORDER_COUPON_DISCOUNT_AMOUNT_MISMATCH.getDefaultMessage() + " 요청: " + command.couponDiscountAmount() + ", 계산: " + couponDiscountAmount);
        }
        if (!command.usePoint().equals(pointDiscountAmount)) {
            throw new BusinessException(ErrorCode.ORDER_POINT_DISCOUNT_AMOUNT_MISMATCH,
                ErrorCode.ORDER_POINT_DISCOUNT_AMOUNT_MISMATCH.getDefaultMessage() + " 요청: " + command.usePoint() + ", 계산: " + pointDiscountAmount);
        }
        if (!command.totalDiscountAmount().equals(totalDiscountAmount)) {
            throw new BusinessException(ErrorCode.ORDER_TOTAL_DISCOUNT_AMOUNT_MISMATCH,
                ErrorCode.ORDER_TOTAL_DISCOUNT_AMOUNT_MISMATCH.getDefaultMessage() + " 요청: " + command.totalDiscountAmount() + ", 계산: " + totalDiscountAmount);
        }
        if (!command.finalAmount().equals(finalAmount)) {
            throw new BusinessException(ErrorCode.ORDER_FINAL_AMOUNT_MISMATCH,
                ErrorCode.ORDER_FINAL_AMOUNT_MISMATCH.getDefaultMessage() + " 요청: " + command.finalAmount() + ", 계산: " + finalAmount);
        }
    }

    private String generateOrderNumber() {
        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        return "ORD-" + dateTime + "-" + uuid;
    }
}
