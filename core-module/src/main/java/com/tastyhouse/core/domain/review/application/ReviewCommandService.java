package com.tastyhouse.core.domain.review.application;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.review.domain.event.ReviewCreatedEvent;
import com.tastyhouse.core.domain.review.domain.event.ReviewDeletedEvent;
import com.tastyhouse.core.domain.review.domain.event.ReviewLikedEvent;
import com.tastyhouse.core.domain.review.domain.model.Review;
import com.tastyhouse.core.domain.review.domain.model.ReviewComment;
import com.tastyhouse.core.domain.review.domain.model.ReviewImage;
import com.tastyhouse.core.domain.review.domain.model.ReviewLike;
import com.tastyhouse.core.domain.review.domain.model.ReviewReply;
import com.tastyhouse.core.domain.review.domain.model.ReviewTag;
import com.tastyhouse.core.domain.review.domain.repository.ReviewCommentRepository;
import com.tastyhouse.core.domain.review.domain.repository.ReviewImageRepository;
import com.tastyhouse.core.domain.review.domain.repository.ReviewLikeRepository;
import com.tastyhouse.core.domain.review.domain.repository.ReviewReplyRepository;
import com.tastyhouse.core.domain.review.domain.repository.ReviewRepository;
import com.tastyhouse.core.domain.review.domain.repository.ReviewTagRepository;
import com.tastyhouse.core.domain.shop.domain.model.Tag;
import com.tastyhouse.core.domain.shop.domain.repository.TagRepository;
import com.tastyhouse.core.domain.review.application.dto.command.ReviewCommentCreateCommand;
import com.tastyhouse.core.domain.review.application.dto.command.ReviewCreateCommand;
import com.tastyhouse.core.domain.review.application.dto.command.ReviewDeleteCommand;
import com.tastyhouse.core.domain.review.application.dto.command.ReviewReplyCreateCommand;
import com.tastyhouse.core.domain.review.application.dto.command.ReviewUpdateCommand;
import com.tastyhouse.core.domain.review.application.dto.command.ToggleReviewLikeCommand;
import com.tastyhouse.core.domain.review.application.dto.result.ReviewResult;
import com.tastyhouse.core.domain.review.domain.vo.ReviewCommentId;
import com.tastyhouse.core.domain.review.domain.vo.ReviewId;
import com.tastyhouse.core.domain.review.domain.vo.ReviewReplyId;
import com.tastyhouse.core.exception.AccessDeniedException;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewCommandService {

    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final ReviewTagRepository reviewTagRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final ReviewCommentRepository reviewCommentRepository;
    private final ReviewReplyRepository reviewReplyRepository;
    private final TagRepository tagRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ReviewResult createReview(ReviewCreateCommand cmd) {
        double totalRating = Math.round(
            (cmd.tasteRating() + cmd.amountRating() + cmd.priceRating()) / 3.0 * 10.0
        ) / 10.0;

        Review review = Review.of(
            cmd.shopId(),
            cmd.productId(),
            cmd.memberId(),
            cmd.content(),
            totalRating,
            cmd.tasteRating().doubleValue(),
            cmd.amountRating().doubleValue(),
            cmd.priceRating().doubleValue(),
            null, null, null, false,
            cmd.orderId()
        );

        Review saved = reviewRepository.save(review);

        List<Long> savedFileIds = saveReviewImages(saved.getId(), cmd.uploadedFileIds());
        List<String> savedTags = saveReviewTags(saved.getId(), cmd.tags());

        eventPublisher.publishEvent(new ReviewCreatedEvent(
            saved.getReviewId(),
            cmd.memberId(),
            cmd.shopId(),
            cmd.productId(),
            LocalDateTime.now()
        ));

        return new ReviewResult(
            saved.getReviewId(),
            saved.getProductId(),
            saved.getTasteRating(),
            saved.getAmountRating(),
            saved.getPriceRating(),
            saved.getTotalRating(),
            saved.getContent(),
            savedFileIds,
            savedTags,
            saved.getCreatedAt()
        );
    }

    public ReviewResult updateReview(ReviewUpdateCommand cmd) {
        Review review = reviewRepository.findByIdAndMemberId(cmd.reviewId(), cmd.memberId())
            .orElseThrow(() -> new AccessDeniedException(ErrorCode.REVIEW_ACCESS_DENIED));

        double totalRating = Math.round(
            (cmd.tasteRating() + cmd.amountRating() + cmd.priceRating()) / 3.0 * 10.0
        ) / 10.0;

        review.updateContent(
            cmd.content(),
            totalRating,
            cmd.tasteRating().doubleValue(),
            cmd.amountRating().doubleValue(),
            cmd.priceRating().doubleValue(),
            null, null, null, false
        );

        reviewRepository.save(review);

        reviewImageRepository.deleteByReviewId(cmd.reviewId().value());
        reviewTagRepository.deleteByReviewId(cmd.reviewId().value());

        List<Long> savedFileIds = saveReviewImages(cmd.reviewId().value(), cmd.uploadedFileIds());
        List<String> savedTags = saveReviewTags(cmd.reviewId().value(), cmd.tags());

        return new ReviewResult(
            review.getReviewId(),
            review.getProductId(),
            review.getTasteRating(),
            review.getAmountRating(),
            review.getPriceRating(),
            review.getTotalRating(),
            review.getContent(),
            savedFileIds,
            savedTags,
            review.getCreatedAt()
        );
    }

    public void deleteReview(ReviewDeleteCommand cmd) {
        reviewRepository.findByIdAndMemberId(cmd.reviewId(), cmd.memberId())
            .orElseThrow(() -> new AccessDeniedException(ErrorCode.REVIEW_ACCESS_DENIED));

        reviewImageRepository.deleteByReviewId(cmd.reviewId().value());
        reviewTagRepository.deleteByReviewId(cmd.reviewId().value());
        reviewRepository.deleteById(cmd.reviewId());

        eventPublisher.publishEvent(new ReviewDeletedEvent(
            cmd.reviewId(),
            cmd.memberId(),
            cmd.productId(),
            LocalDateTime.now()
        ));
    }

    public boolean toggleReviewLike(ToggleReviewLikeCommand cmd) {
        boolean alreadyLiked = reviewLikeRepository.existsByReviewIdAndMemberId(cmd.reviewId(), cmd.memberId());

        if (alreadyLiked) {
            reviewLikeRepository.deleteByReviewIdAndMemberId(cmd.reviewId(), cmd.memberId());
            eventPublisher.publishEvent(new ReviewLikedEvent(
                cmd.reviewId(),
                cmd.memberId(),
                false,
                LocalDateTime.now()
            ));
            return false;
        } else {
            reviewLikeRepository.save(ReviewLike.of(cmd.reviewId().value(), cmd.memberId()));
            eventPublisher.publishEvent(new ReviewLikedEvent(
                cmd.reviewId(),
                cmd.memberId(),
                true,
                LocalDateTime.now()
            ));
            return true;
        }
    }

    public void changeReviewHidden(ReviewId reviewId, boolean hidden) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.REVIEW_NOT_FOUND));

        if (hidden) {
            review.hide();
        } else {
            review.unhide();
        }

        reviewRepository.save(review);
    }

    public void deleteReview(ReviewId reviewId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.REVIEW_NOT_FOUND));

        reviewImageRepository.deleteByReviewId(reviewId.value());
        reviewTagRepository.deleteByReviewId(reviewId.value());
        reviewRepository.deleteById(reviewId);

        eventPublisher.publishEvent(new ReviewDeletedEvent(
            reviewId,
            review.getMemberId(),
            review.getProductId(),
            LocalDateTime.now()
        ));
    }

    public void changeCommentHidden(ReviewCommentId commentId, boolean hidden) {
        ReviewComment comment = reviewCommentRepository.findById(commentId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.REVIEW_COMMENT_NOT_FOUND));

        if (hidden) {
            comment.hide();
        } else {
            comment.unhide();
        }

        reviewCommentRepository.save(comment);
    }

    public void deleteComment(ReviewCommentId commentId) {
        reviewCommentRepository.findById(commentId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.REVIEW_COMMENT_NOT_FOUND));

        reviewCommentRepository.deleteById(commentId);
    }

    public void changeReplyHidden(ReviewReplyId replyId, boolean hidden) {
        ReviewReply reply = reviewReplyRepository.findById(replyId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.REVIEW_REPLY_NOT_FOUND));

        if (hidden) {
            reply.hide();
        } else {
            reply.unhide();
        }

        reviewReplyRepository.save(reply);
    }

    public void deleteReply(ReviewReplyId replyId) {
        reviewReplyRepository.findById(replyId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.REVIEW_REPLY_NOT_FOUND));

        reviewReplyRepository.deleteById(replyId);
    }

    public ReviewComment createComment(ReviewCommentCreateCommand cmd) {
        return reviewCommentRepository.save(ReviewComment.of(cmd.reviewId().value(), cmd.memberId(), cmd.content()));
    }

    public ReviewReply createReply(ReviewReplyCreateCommand cmd) {
        return reviewReplyRepository.save(ReviewReply.of(cmd.commentId().value(), cmd.memberId(), cmd.replyToMemberId(), cmd.content()));
    }

    private List<Long> saveReviewImages(Long reviewId, List<Long> uploadedFileIds) {
        if (uploadedFileIds == null || uploadedFileIds.isEmpty()) {
            return List.of();
        }
        List<ReviewImage> images = new ArrayList<>();
        for (int i = 0; i < uploadedFileIds.size(); i++) {
            images.add(ReviewImage.of(reviewId, uploadedFileIds.get(i), i + 1));
        }
        reviewImageRepository.saveAll(images);
        return uploadedFileIds;
    }

    private List<String> saveReviewTags(Long reviewId, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return List.of();
        }
        List<ReviewTag> reviewTags = tagNames.stream()
            .map(tagName -> {
                Tag tag = tagRepository.findByTagName(tagName)
                    .orElseGet(() -> tagRepository.save(Tag.of(tagName)));
                return ReviewTag.of(reviewId, tag.getId());
            })
            .toList();
        reviewTagRepository.saveAll(reviewTags);
        return tagNames;
    }
}
