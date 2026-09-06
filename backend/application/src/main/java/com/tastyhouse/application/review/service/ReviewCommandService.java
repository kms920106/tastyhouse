package com.tastyhouse.application.review.service;

import com.tastyhouse.application.shared.marker.WebApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.model.Order;
import com.tastyhouse.domain.order.model.OrderProduct;
import com.tastyhouse.domain.order.repository.OrderProductRepository;
import com.tastyhouse.domain.order.repository.OrderRepository;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.order.vo.OrderProductId;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.review.model.Review;
import com.tastyhouse.domain.review.model.ReviewComment;
import com.tastyhouse.domain.review.model.ReviewReply;
import com.tastyhouse.domain.review.repository.ReviewCommentRepository;
import com.tastyhouse.domain.review.repository.ReviewReplyRepository;
import com.tastyhouse.domain.review.repository.ReviewRepository;
import com.tastyhouse.domain.review.service.ReviewLifecycleService;
import com.tastyhouse.domain.review.service.ReviewRegistration;
import com.tastyhouse.domain.review.vo.ReviewCommentId;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.application.review.port.in.ReviewCommandUseCase;
import com.tastyhouse.application.review.port.in.ReviewCommentCreateCommand;
import com.tastyhouse.application.review.port.in.ReviewCreateCommand;
import com.tastyhouse.application.review.port.in.ReviewDeleteCommand;
import com.tastyhouse.application.review.port.in.ReviewLikeToggleCommand;
import com.tastyhouse.application.review.port.in.ReviewReplyCreateCommand;
import com.tastyhouse.application.review.port.in.ReviewUpdateCommand;

@Service
@WebApp
@Transactional
public class ReviewCommandService implements ReviewCommandUseCase {

    private final ReviewLifecycleService reviewLifecycleService;
    private final ReviewRepository reviewRepository;
    private final ReviewCommentRepository reviewCommentRepository;
    private final ReviewReplyRepository reviewReplyRepository;
    private final ProductRepository productRepository;
    private final OrderProductRepository orderProductRepository;
    private final OrderRepository orderRepository;

    public ReviewCommandService(
        ReviewLifecycleService reviewLifecycleService,
        ReviewRepository reviewRepository,
        ReviewCommentRepository reviewCommentRepository,
        ReviewReplyRepository reviewReplyRepository,
        ProductRepository productRepository,
        OrderProductRepository orderProductRepository,
        OrderRepository orderRepository
    ) {
        this.reviewLifecycleService = reviewLifecycleService;
        this.reviewRepository = reviewRepository;
        this.reviewCommentRepository = reviewCommentRepository;
        this.reviewReplyRepository = reviewReplyRepository;
        this.productRepository = productRepository;
        this.orderProductRepository = orderProductRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public Long createReview(ReviewCreateCommand command) {
        Long memberId = command.memberId();
        Long orderProductId = command.orderProductId();
        Integer deliveryRating = command.deliveryRating();
        String deliveryComment = command.deliveryComment();

        OrderId orderId = null;
        if (orderProductId != null) {
            OrderProduct orderProduct = orderProductRepository.findById(OrderProductId.of(orderProductId))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_ORDER_PRODUCT_NOT_FOUND));
            orderId = orderProduct.getOrderId();
            validateOrderOwnership(orderId, MemberId.of(memberId));
        }
        validateDeliveryRating(orderId, deliveryRating, deliveryComment);

        Product product = productRepository.findById(ProductId.of(command.productId()))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_PRODUCT_NOT_FOUND));

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
        Review review = reviewRepository.findById(targetReviewId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_NOT_FOUND));
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
        Review review = reviewRepository.findById(targetReviewId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_NOT_FOUND));

        reviewLifecycleService.removeOwnedBy(targetReviewId, MemberId.of(command.memberId()), review.getProductId());
    }

    private void validateOrderOwnership(OrderId orderId, MemberId memberId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND));
        if (!order.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.REVIEW_ORDER_ACCESS_DENIED);
        }
    }

    private void validateDeliveryRating(OrderId orderId, Integer deliveryRating, String deliveryComment) {
        if (deliveryRating == null && deliveryComment == null) {
            return;
        }
        if (orderId == null) {
            throw new BusinessException(ErrorCode.REVIEW_DELIVERY_RATING_NOT_ALLOWED);
        }

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND));
        if (order.getOrderMethod() != OrderMethod.DELIVERY) {
            throw new BusinessException(ErrorCode.REVIEW_DELIVERY_RATING_NOT_ALLOWED);
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
        ReviewComment comment = reviewCommentRepository.save(
            ReviewComment.of(targetReviewId, MemberId.of(command.memberId()), command.content())
        );
        return comment.getId();
    }

    @Override
    public Long findReviewIdOfComment(Long commentId) {
        return reviewCommentRepository.findById(ReviewCommentId.of(commentId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_COMMENT_NOT_FOUND))
            .getReviewId()
            .value();
    }

    @Override
    public Long createReply(ReviewReplyCreateCommand command) {
        Long replyToMemberId = command.replyToMemberId();
        ReviewCommentId reviewCommentId = ReviewCommentId.of(command.commentId());
        ReviewReply reply = reviewReplyRepository.save(ReviewReply.of(
            reviewCommentId,
            MemberId.of(command.memberId()),
            replyToMemberId == null ? null : MemberId.of(replyToMemberId),
            command.content()
        ));

        return reply.getId();
    }
}
