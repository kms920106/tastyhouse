package com.tastyhouse.application.review.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.model.Order;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.review.model.Review;
import com.tastyhouse.domain.review.model.ReviewRegistration;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.application.order.port.out.write.OrderPersistencePort;
import com.tastyhouse.application.review.port.in.ReviewUpdateCommand;
import com.tastyhouse.application.review.port.in.ReviewUpdateUseCase;
import com.tastyhouse.application.review.port.out.write.ReviewPersistencePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shared.exception.WebErrorCode;

@Service
@Transactional
class ReviewUpdateService implements ReviewUpdateUseCase {

    private final ReviewLifecycleService reviewLifecycleService;
    private final ReviewPersistencePort reviewPersistencePort;
    private final OrderPersistencePort orderPersistencePort;

    public ReviewUpdateService(
        ReviewLifecycleService reviewLifecycleService,
        ReviewPersistencePort reviewPersistencePort,
        OrderPersistencePort orderPersistencePort
    ) {
        this.reviewLifecycleService = reviewLifecycleService;
        this.reviewPersistencePort = reviewPersistencePort;
        this.orderPersistencePort = orderPersistencePort;
    }

    @Override
    public Long updateReview(ReviewUpdateCommand command) {
        Integer deliveryRating = command.deliveryRating();
        String deliveryComment = command.deliveryComment();

        ReviewId targetReviewId = ReviewId.of(command.reviewId());
        Review review = reviewPersistencePort.findById(targetReviewId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.REVIEW_NOT_FOUND));
        validateDeliveryRating(review.getOrderId(), deliveryRating, deliveryComment);

        ReviewRegistration registration = reviewLifecycleService.modify(
            targetReviewId,
            MemberId.of(command.memberId()),
            command.tasteRating(),
            command.amountRating(),
            command.priceRating(),
            command.content(),
            command.uploadedFileIds(),
            command.tags(),
            deliveryRating,
            deliveryComment
        );

        return registration.review().getReviewId().value();
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
