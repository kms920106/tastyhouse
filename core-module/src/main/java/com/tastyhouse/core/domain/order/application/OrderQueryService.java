package com.tastyhouse.core.domain.order.application;

import com.tastyhouse.core.domain.order.application.dto.result.OrderItemOptionResult;
import com.tastyhouse.core.domain.order.application.dto.result.OrderItemResult;
import com.tastyhouse.core.domain.order.application.dto.result.OrderListItemResult;
import com.tastyhouse.core.domain.order.application.dto.result.OrderResult;
import com.tastyhouse.core.domain.order.domain.model.Order;
import com.tastyhouse.core.domain.order.domain.model.OrderItem;
import com.tastyhouse.core.domain.order.domain.model.OrderItemOption;
import com.tastyhouse.core.domain.order.domain.repository.OrderItemOptionRepository;
import com.tastyhouse.core.domain.order.domain.repository.OrderItemRepository;
import com.tastyhouse.core.domain.order.domain.repository.OrderRepository;
import com.tastyhouse.core.domain.payment.domain.model.Payment;
import com.tastyhouse.core.domain.payment.domain.repository.PaymentRepository;
import com.tastyhouse.core.domain.order.domain.vo.OrderId;
import com.tastyhouse.core.domain.place.application.PlaceQueryService;
import com.tastyhouse.core.domain.place.domain.model.Place;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderItemOptionRepository orderItemOptionRepository;
    private final PaymentRepository paymentRepository;
    private final PlaceQueryService placeQueryService;

    public Optional<Order> findById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    public Page<OrderListItemResult> findOrderList(Long memberId, int page, int size) {
        return orderRepository.findOrderListByMemberId(memberId, PageRequest.of(page, size));
    }

    public OrderResult findOrderDetail(Long memberId, Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ORDER_NOT_FOUND));

        order.validateOwnership(memberId);

        Place place = placeQueryService.findPlaceById(order.getPlaceId());

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        List<OrderItemResult> itemResults = items.stream()
            .map(item -> {
                List<OrderItemOption> options = orderItemOptionRepository.findByOrderItemId(item.getId());
                List<OrderItemOptionResult> optionResults = options.stream()
                    .map(OrderItemOptionResult::from)
                    .toList();
                return new OrderItemResult(
                    item.getId(),
                    item.getProductId(),
                    item.getProductName(),
                    item.getProductImageUrl(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getDiscountPrice(),
                    item.getOptionTotalPrice(),
                    item.getTotalPrice(),
                    optionResults
                );
            })
            .toList();

        Payment payment = paymentRepository.findByOrderId(new OrderId(orderId)).orElse(null);

        return OrderResult.from(
            order,
            place != null ? place.getName() : null,
            place != null ? place.getPhoneNumber() : null,
            itemResults,
            payment
        );
    }

    public OrderItem findOrderItemById(Long orderItemId) {
        return orderItemRepository.findById(orderItemId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.REVIEW_ORDER_ITEM_NOT_FOUND));
    }

    public List<OrderItem> findOrderItems(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    public List<OrderItemOption> findOrderItemOptions(Long orderItemId) {
        return orderItemOptionRepository.findByOrderItemId(orderItemId);
    }

    public Optional<Payment> findPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(new OrderId(orderId));
    }
}
