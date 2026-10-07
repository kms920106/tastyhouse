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
import com.tastyhouse.application.order.port.out.write.OrderPersistencePort;
import com.tastyhouse.application.order.port.out.write.OrderProductPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductPersistencePort;
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
    private final ProductPersistencePort productPersistencePort;
    private final OrderProductPersistencePort orderProductPersistencePort;
    private final OrderPersistencePort orderPersistencePort;

    public ReviewCreateService(
        ReviewLifecycleService reviewLifecycleService,
        ProductPersistencePort productPersistencePort,
        OrderProductPersistencePort orderProductPersistencePort,
        OrderPersistencePort orderPersistencePort
    ) {
        this.reviewLifecycleService = reviewLifecycleService;
        this.productPersistencePort = productPersistencePort;
        this.orderProductPersistencePort = orderProductPersistencePort;
        this.orderPersistencePort = orderPersistencePort;
    }

    @Override
    public Long createReview(ReviewCreateCommand command) {
        Long memberId = command.memberId();
        Long orderProductId = command.orderProductId();
        Integer deliveryRating = command.deliveryRating();
        String deliveryComment = command.deliveryComment();

        OrderId orderId = null;
        if (orderProductId != null) {
            OrderProduct orderProduct = orderProductPersistencePort.findById(OrderProductId.of(orderProductId))
                .orElseThrow(() -> new ResourceNotFoundException(WebErrorCode.REVIEW_ORDER_PRODUCT_NOT_FOUND));
            orderId = orderProduct.getOrderId();
            validateOrderOwnership(orderId, MemberId.of(memberId));
        }
        validateDeliveryRating(orderId, deliveryRating, deliveryComment);

        Product product = productPersistencePort.findById(ProductId.of(command.productId()))
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
        Order order = orderPersistencePort.findById(orderId)
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

        Order order = orderPersistencePort.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.ORDER_NOT_FOUND));
        if (order.getOrderMethod() != OrderMethod.DELIVERY) {
            throw new ApplicationException(WebErrorCode.REVIEW_DELIVERY_RATING_NOT_ALLOWED);
        }
    }
}
