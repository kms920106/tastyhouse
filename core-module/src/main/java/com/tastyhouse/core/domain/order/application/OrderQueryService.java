package com.tastyhouse.core.domain.order.application;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.order.domain.model.Order;
import com.tastyhouse.core.domain.order.domain.model.OrderProduct;
import com.tastyhouse.core.domain.order.domain.model.OrderProductOption;
import com.tastyhouse.core.domain.order.domain.repository.OrderProductOptionRepository;
import com.tastyhouse.core.domain.order.domain.repository.OrderProductRepository;
import com.tastyhouse.core.domain.order.domain.repository.OrderRepository;
import com.tastyhouse.core.domain.order.domain.vo.OrderId;
import com.tastyhouse.core.domain.order.domain.vo.OrderProductId;
import com.tastyhouse.core.domain.payment.domain.model.Payment;
import com.tastyhouse.core.domain.payment.domain.repository.PaymentRepository;
import com.tastyhouse.core.domain.shop.domain.model.Shop;
import com.tastyhouse.core.domain.shop.domain.vo.ShopId;
import com.tastyhouse.core.domain.order.application.dto.OrderSearchCondition;
import com.tastyhouse.core.domain.order.application.dto.result.OrderAdminListItemResult;
import com.tastyhouse.core.domain.order.application.dto.result.OrderListItemResult;
import com.tastyhouse.core.domain.order.application.dto.result.OrderProductOptionResult;
import com.tastyhouse.core.domain.order.application.dto.result.OrderProductResult;
import com.tastyhouse.core.domain.order.application.dto.result.OrderResult;
import com.tastyhouse.core.domain.shop.application.ShopQueryService;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final OrderProductOptionRepository orderProductOptionRepository;
    private final PaymentRepository paymentRepository;
    private final ShopQueryService shopQueryService;

    public Optional<Order> findById(OrderId orderId) {
        return orderRepository.findById(orderId);
    }

    public PageResult<OrderListItemResult> findOrderList(MemberId memberId, int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return orderRepository.findOrderListByMemberId(memberId, pageQuery);
    }

    public PageResult<OrderAdminListItemResult> findOrders(OrderSearchCondition condition, int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return orderRepository.findOrders(condition, pageQuery);
    }

    public OrderResult findOrderDetail(MemberId memberId, OrderId orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ORDER_NOT_FOUND));

        order.validateOwnership(memberId);

        Shop shop = shopQueryService.findShopById(ShopId.of(order.getShopId()));

        List<OrderProductResult> itemResults = buildOrderProductResults(orderId);

        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);

        return OrderResult.from(
            order,
            shop != null ? shop.getName() : null,
            shop != null ? shop.getPhoneNumber() : null,
            itemResults,
            payment
        );
    }

    public OrderResult findOrderDetailById(OrderId orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ORDER_NOT_FOUND));

        Shop shop = shopQueryService.findShopById(ShopId.of(order.getShopId()));

        List<OrderProductResult> itemResults = buildOrderProductResults(orderId);

        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);

        return OrderResult.from(
            order,
            shop != null ? shop.getName() : null,
            shop != null ? shop.getPhoneNumber() : null,
            itemResults,
            payment
        );
    }

    private List<OrderProductResult> buildOrderProductResults(OrderId orderId) {
        List<OrderProduct> items = orderProductRepository.findByOrderId(orderId);
        return items.stream()
            .map(item -> {
                List<OrderProductOption> options = orderProductOptionRepository.findByOrderProductId(item.getOrderProductId());
                List<OrderProductOptionResult> optionResults = options.stream()
                    .map(OrderProductOptionResult::from)
                    .toList();
                return new OrderProductResult(
                    item.getOrderProductId(),
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

    public OrderProduct findOrderProductById(OrderProductId orderProductId) {
        return orderProductRepository.findById(orderProductId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.REVIEW_ORDER_PRODUCT_NOT_FOUND));
    }
}
