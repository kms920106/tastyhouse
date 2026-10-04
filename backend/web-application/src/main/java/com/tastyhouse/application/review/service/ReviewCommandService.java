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
import com.tastyhouse.domain.review.model.Review;
import com.tastyhouse.domain.review.model.ReviewComment;
import com.tastyhouse.domain.review.model.ReviewRegistration;
import com.tastyhouse.domain.review.model.ReviewReply;
import com.tastyhouse.domain.review.vo.ReviewCommentId;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.application.order.port.out.write.OrderPersistencePort;
import com.tastyhouse.application.order.port.out.write.OrderProductPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductPersistencePort;
import com.tastyhouse.application.review.port.in.ReviewCommandUseCase;
import com.tastyhouse.application.review.port.in.ReviewCommentCreateCommand;
import com.tastyhouse.application.review.port.in.ReviewCreateCommand;
import com.tastyhouse.application.review.port.in.ReviewDeleteCommand;
import com.tastyhouse.application.review.port.in.ReviewLikeToggleCommand;
import com.tastyhouse.application.review.port.in.ReviewReplyCreateCommand;
import com.tastyhouse.application.review.port.in.ReviewUpdateCommand;
import com.tastyhouse.application.review.port.out.write.ReviewCommentPersistencePort;
import com.tastyhouse.application.review.port.out.write.ReviewPersistencePort;
import com.tastyhouse.application.review.port.out.write.ReviewReplyPersistencePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shared.exception.WebErrorCode;

@Service
@Transactional
class ReviewCommandService implements ReviewCommandUseCase {

    private final ReviewLifecycleService reviewLifecycleService;
    private final ReviewPersistencePort reviewPersistencePort;
    private final ReviewCommentPersistencePort reviewCommentPersistencePort;
    private final ReviewReplyPersistencePort reviewReplyPersistencePort;
    private final ProductPersistencePort productPersistencePort;
    private final OrderProductPersistencePort orderProductPersistencePort;
    private final OrderPersistencePort orderPersistencePort;

    public ReviewCommandService(
        ReviewLifecycleService reviewLifecycleService,
        ReviewPersistencePort reviewPersistencePort,
        ReviewCommentPersistencePort reviewCommentPersistencePort,
        ReviewReplyPersistencePort reviewReplyPersistencePort,
        ProductPersistencePort productPersistencePort,
        OrderProductPersistencePort orderProductPersistencePort,
        OrderPersistencePort orderPersistencePort
    ) {
        this.reviewLifecycleService = reviewLifecycleService;
        this.reviewPersistencePort = reviewPersistencePort;
        this.reviewCommentPersistencePort = reviewCommentPersistencePort;
        this.reviewReplyPersistencePort = reviewReplyPersistencePort;
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

    @Override
    public void deleteReview(ReviewDeleteCommand command) {
        ReviewId targetReviewId = ReviewId.of(command.reviewId());
        Review review = reviewPersistencePort.findById(targetReviewId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.REVIEW_NOT_FOUND));

        reviewLifecycleService.removeOwnedBy(targetReviewId, MemberId.of(command.memberId()), review.getProductId());
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

    @Override
    public boolean toggleReviewLike(ReviewLikeToggleCommand command) {
        ReviewId targetReviewId = ReviewId.of(command.reviewId());
        return reviewLifecycleService.toggleLike(targetReviewId, MemberId.of(command.memberId()));
    }

    @Override
    public Long createComment(ReviewCommentCreateCommand command) {
        ReviewId targetReviewId = ReviewId.of(command.reviewId());
        ReviewComment comment = reviewCommentPersistencePort.save(
            ReviewComment.of(targetReviewId, MemberId.of(command.memberId()), command.content())
        );
        return comment.getId();
    }

    @Override
    public Long findReviewIdOfComment(Long commentId) {
        return reviewCommentPersistencePort.findById(ReviewCommentId.of(commentId))
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.REVIEW_COMMENT_NOT_FOUND))
            .getReviewId()
            .value();
    }

    @Override
    public Long createReply(ReviewReplyCreateCommand command) {
        Long replyToMemberId = command.replyToMemberId();
        ReviewCommentId reviewCommentId = ReviewCommentId.of(command.commentId());
        ReviewReply reply = reviewReplyPersistencePort.save(ReviewReply.of(
            reviewCommentId,
            MemberId.of(command.memberId()),
            replyToMemberId == null ? null : MemberId.of(replyToMemberId),
            command.content()
        ));

        return reply.getId();
    }
}
