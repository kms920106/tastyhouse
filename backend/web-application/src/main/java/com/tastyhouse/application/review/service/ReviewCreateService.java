package com.tastyhouse.application.review.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.model.Order;
import com.tastyhouse.domain.order.model.OrderProduct;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.order.vo.OrderProductId;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.review.model.ReviewRegistration;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.application.order.port.out.write.OrderLoadPort;
import com.tastyhouse.application.order.port.out.write.OrderProductLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductLoadPort;
import com.tastyhouse.application.review.port.in.ReviewCreateCommand;
import com.tastyhouse.application.review.port.in.ReviewCreateUseCase;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shared.exception.WebErrorCode;

@Service
@Transactional
class ReviewCreateService implements ReviewCreateUseCase {

    private final ReviewLifecycleService reviewLifecycleService;
    private final ProductLoadPort productLoadPort;
    private final OrderProductLoadPort orderProductLoadPort;
    private final OrderLoadPort orderLoadPort;

    public ReviewCreateService(
        ReviewLifecycleService reviewLifecycleService,
        ProductLoadPort productLoadPort,
        OrderProductLoadPort orderProductLoadPort,
        OrderLoadPort orderLoadPort
    ) {
        this.reviewLifecycleService = reviewLifecycleService;
        this.productLoadPort = productLoadPort;
        this.orderProductLoadPort = orderProductLoadPort;
        this.orderLoadPort = orderLoadPort;
    }

    @Override
    public Long createReview(ReviewCreateCommand command) {
        Long memberId = command.memberId();
        Long orderProductId = command.orderProductId();
        Integer deliveryRating = command.deliveryRating();
        String deliveryComment = command.deliveryComment();

        OrderId orderId = null;
        if (orderProductId != null) {
            OrderProduct orderProduct = orderProductLoadPort.findById(OrderProductId.of(orderProductId))
                .orElseThrow(() -> new ResourceNotFoundException(WebErrorCode.REVIEW_ORDER_PRODUCT_NOT_FOUND));
            orderId = orderProduct.getOrderId();
            validateOrderOwnership(orderId, MemberId.of(memberId));
        }
        validateDeliveryRating(orderId, deliveryRating, deliveryComment);

        Product product = productLoadPort.findById(ProductId.of(command.productId()))
            .orElseThrow(() -> new ResourceNotFoundException(WebErrorCode.ORDER_PRODUCT_NOT_FOUND));

        ReviewRegistration registration = reviewLifecycleService.register(
            product.getShopId(),
            product.getProductId(),
            MemberId.of(memberId),
            orderId,
            command.tasteRating(),
            command.amountRating(),
            command.priceRating(),
            command.content(),
            command.uploadedFileIds(),
            command.tags(),
            Boolean.TRUE.equals(command.ownerOnly()),
            deliveryRating,
            deliveryComment
        );

        return registration.review().getReviewId().value();
    }

    private void validateOrderOwnership(OrderId orderId, MemberId memberId) {
        Order order = orderLoadPort.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.ORDER_NOT_FOUND));
        if (!order.getMemberId().equals(memberId)) {
            throw new ApplicationException(WebErrorCode.REVIEW_ORDER_ACCESS_DENIED);
        }
    }

    private void validateDeliveryRating(OrderId orderId, Integer deliveryRating, String deliveryComment) {
        if (deliveryRating == null && deliveryComment == null) {
            return;
        }
        if (orderId == null) {
            throw new ApplicationException(WebErrorCode.REVIEW_DELIVERY_RATING_NOT_ALLOWED);
        }

        Order order = orderLoadPort.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.ORDER_NOT_FOUND));
        if (order.getOrderMethod() != OrderMethod.DELIVERY) {
            throw new ApplicationException(WebErrorCode.REVIEW_DELIVERY_RATING_NOT_ALLOWED);
        }
    }
}
