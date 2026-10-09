package com.tastyhouse.application.review.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.review.event.ReviewOwnerReplyCreatedEvent;
import com.tastyhouse.domain.review.model.Review;
import com.tastyhouse.domain.review.model.ReviewOwnerReply;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.review.vo.ReviewOwnerReplyId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.review.port.out.write.ReviewLoadPort;
import com.tastyhouse.application.review.port.out.write.ReviewOwnerReplyLoadPort;
import com.tastyhouse.application.review.port.out.write.ReviewOwnerReplySavePort;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.CeoErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.service.ProhibitedWordValidator;

@Service
public class ReviewOwnerReplyService {

    private final ReviewOwnerReplyLoadPort reviewOwnerReplyLoadPort;
    private final ReviewOwnerReplySavePort reviewOwnerReplySavePort;
    private final ReviewLoadPort reviewLoadPort;
    private final ProhibitedWordValidator prohibitedWordValidator;
    private final DomainEventPublisher domainEventPublisher;

    public ReviewOwnerReplyService(
        ReviewOwnerReplyLoadPort reviewOwnerReplyLoadPort,
        ReviewOwnerReplySavePort reviewOwnerReplySavePort,
        ReviewLoadPort reviewLoadPort,
        ProhibitedWordValidator prohibitedWordValidator,
        DomainEventPublisher domainEventPublisher
    ) {
        this.reviewOwnerReplyLoadPort = reviewOwnerReplyLoadPort;
        this.reviewOwnerReplySavePort = reviewOwnerReplySavePort;
        this.reviewLoadPort = reviewLoadPort;
        this.prohibitedWordValidator = prohibitedWordValidator;
        this.domainEventPublisher = domainEventPublisher;
    }

    public Long register(Long shopId, Long reviewId, Long ceoId, String content, LocalDate today) {
        ReviewId targetReviewId = ReviewId.of(reviewId);
        Review review = loadReviewOfShop(targetReviewId, shopId);
        validateWithinReplyPeriod(review, today);
        prohibitedWordValidator.validate(content);

        if (reviewOwnerReplyLoadPort.existsByReviewId(targetReviewId)) {
            throw new ApplicationException(CeoErrorCode.REVIEW_OWNER_REPLY_ALREADY_EXISTS);
        }

        ReviewOwnerReply saved = reviewOwnerReplySavePort.save(
            ReviewOwnerReply.of(targetReviewId, ShopId.of(shopId), CeoId.of(ceoId), content)
        );

        domainEventPublisher.publish(new ReviewOwnerReplyCreatedEvent(
            targetReviewId,
            review.getMemberId(),
            ShopId.of(shopId),
            ReviewOwnerReplyId.of(saved.getId()),
            LocalDateTime.now()
        ));

        return saved.getId();
    }

    private void validateWithinReplyPeriod(Review review, LocalDate today) {
        LocalDate deadline = review.getCreatedAt().toLocalDate().plusDays(ReviewOwnerReply.REPLY_PERIOD_DAYS);
        if (today.isAfter(deadline)) {
            throw new ApplicationException(CeoErrorCode.REVIEW_OWNER_REPLY_PERIOD_EXPIRED);
        }
    }

    public void modify(Long shopId, Long reviewId, String content) {
        ReviewId targetReviewId = ReviewId.of(reviewId);
        loadReviewOfShop(targetReviewId, shopId);
        prohibitedWordValidator.validate(content);

        ReviewOwnerReply reply = loadReplyOfReview(targetReviewId);
        reply.updateContent(content);
        reviewOwnerReplySavePort.save(reply);
    }

    public void remove(Long shopId, Long reviewId) {
        ReviewId targetReviewId = ReviewId.of(reviewId);
        loadReviewOfShop(targetReviewId, shopId);

        ReviewOwnerReply reply = loadReplyOfReview(targetReviewId);
        reviewOwnerReplySavePort.delete(reply);
    }

    private Review loadReviewOfShop(ReviewId reviewId, Long shopId) {
        Review review = reviewLoadPort.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.REVIEW_NOT_FOUND));
        if (!review.getShopId().equals(ShopId.of(shopId))) {
            throw new ApplicationException(ApplicationErrorCode.SHOP_ACCESS_DENIED);
        }
        return review;
    }

    private ReviewOwnerReply loadReplyOfReview(ReviewId reviewId) {
        return reviewOwnerReplyLoadPort.findByReviewId(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException(CeoErrorCode.REVIEW_OWNER_REPLY_NOT_FOUND));
    }
}
