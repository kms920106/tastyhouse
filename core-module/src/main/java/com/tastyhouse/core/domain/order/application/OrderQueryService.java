package com.tastyhouse.core.domain.order.application;

import com.tastyhouse.core.domain.order.application.dto.result.OrderProductOptionResult;
import com.tastyhouse.core.domain.order.application.dto.result.OrderProductResult;
import com.tastyhouse.core.domain.order.application.dto.result.OrderListItemResult;
import com.tastyhouse.core.domain.order.application.dto.result.OrderResult;
import com.tastyhouse.core.domain.order.domain.model.Order;
import com.tastyhouse.core.domain.order.domain.model.OrderProduct;
import com.tastyhouse.core.domain.order.domain.model.OrderProductOption;
import com.tastyhouse.core.domain.order.domain.repository.OrderProductOptionRepository;
import com.tastyhouse.core.domain.order.domain.repository.OrderProductRepository;
import com.tastyhouse.core.domain.order.domain.repository.OrderRepository;
import com.tastyhouse.core.domain.payment.domain.model.Payment;
import com.tastyhouse.core.domain.payment.domain.repository.PaymentRepository;
import com.tastyhouse.core.domain.order.domain.vo.OrderId;
import com.tastyhouse.core.domain.shop.application.ShopQueryService;
import com.tastyhouse.core.domain.shop.domain.model.Shop;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final OrderProductOptionRepository orderProductOptionRepository;
    private final PaymentRepository paymentRepository;
    private final ShopQueryService shopQueryService;

    public Optional<Order> findById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    public PageResult<OrderListItemResult> findOrderList(Long memberId, int page, int size) {
        return orderRepository.findOrderListByMemberId(memberId, PageQuery.of(page, size));
    }

    public OrderResult findOrderDetail(Long memberId, Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ORDER_NOT_FOUND));

        order.validateOwnership(memberId);

        Shop shop = shopQueryService.findShopById(order.getShopId());

        List<OrderProduct> items = orderProductRepository.findByOrderId(orderId);
        List<OrderProductResult> itemResults = items.stream()
            .map(item -> {
                List<OrderProductOption> options = orderProductOptionRepository.findByOrderProductId(item.getId());
                List<OrderProductOptionResult> optionResults = options.stream()
                    .map(OrderProductOptionResult::from)
                    .toList();
                return new OrderProductResult(
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

        Payment payment = paymentRepository.findByOrderId(new OrderId(orderId)).orElse(null);

        return OrderResult.from(
            order,
            shop != null ? shop.getName() : null,
            shop != null ? shop.getPhoneNumber() : null,
            itemResults,
            payment
        );
    }

    public OrderProduct findOrderProductById(Long orderProductId) {
        return orderProductRepository.findById(orderProductId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.REVIEW_ORDER_PRODUCT_NOT_FOUND));
    }
}
