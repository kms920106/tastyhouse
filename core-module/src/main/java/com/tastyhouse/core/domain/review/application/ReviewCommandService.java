package com.tastyhouse.core.domain.review.application;

import com.tastyhouse.core.domain.review.application.dto.command.CreateReviewCommand;
import com.tastyhouse.core.domain.review.application.dto.command.CreateReviewCommentCommand;
import com.tastyhouse.core.domain.review.application.dto.command.CreateReviewReplyCommand;
import com.tastyhouse.core.domain.review.application.dto.command.DeleteReviewCommand;
import com.tastyhouse.core.domain.review.application.dto.command.ToggleReviewLikeCommand;
import com.tastyhouse.core.domain.review.application.dto.command.UpdateReviewCommand;
import com.tastyhouse.core.domain.review.application.dto.result.ReviewResult;
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
import com.tastyhouse.core.domain.review.domain.vo.ReviewId;
import com.tastyhouse.core.domain.place.domain.model.Tag;
import com.tastyhouse.core.exception.AccessDeniedException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.domain.place.domain.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    // TODO: Place BC 미전환 과도기 허용 — Place 도메인 DDD 전환 후 PlaceTagQueryService 참조로 교체
    private final TagRepository tagRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ReviewResult createReview(CreateReviewCommand cmd) {
        double totalRating = Math.round(
            (cmd.tasteRating() + cmd.amountRating() + cmd.priceRating()) / 3.0 * 10.0
        ) / 10.0;

        Review review = Review.of(
            cmd.placeId(),
            cmd.productId(),
            cmd.memberId(),
            cmd.content(),
            totalRating,
            cmd.tasteRating().doubleValue(),
            cmd.amountRating().doubleValue(),
            cmd.priceRating().doubleValue(),
            null, null, null, null,
            cmd.orderId()
        );

        Review saved = reviewRepository.save(review);

        List<Long> savedFileIds = saveReviewImages(saved.getId(), cmd.uploadedFileIds());
        List<String> savedTags = saveReviewTags(saved.getId(), cmd.tags());

        eventPublisher.publishEvent(new ReviewCreatedEvent(
            new ReviewId(saved.getId()),
            cmd.memberId(),
            cmd.placeId(),
            cmd.productId(),
            LocalDateTime.now()
        ));

        return new ReviewResult(
            saved.getId(),
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

    public ReviewResult updateReview(UpdateReviewCommand cmd) {
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
            null, null, null, null
        );

        reviewImageRepository.deleteByReviewId(cmd.reviewId());
        reviewTagRepository.deleteByReviewId(cmd.reviewId());

        List<Long> savedFileIds = saveReviewImages(cmd.reviewId(), cmd.uploadedFileIds());
        List<String> savedTags = saveReviewTags(cmd.reviewId(), cmd.tags());

        return new ReviewResult(
            review.getId(),
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

    public void deleteReview(DeleteReviewCommand cmd) {
        reviewRepository.findByIdAndMemberId(cmd.reviewId(), cmd.memberId())
            .orElseThrow(() -> new AccessDeniedException(ErrorCode.REVIEW_ACCESS_DENIED));

        reviewImageRepository.deleteByReviewId(cmd.reviewId());
        reviewTagRepository.deleteByReviewId(cmd.reviewId());
        reviewRepository.deleteById(cmd.reviewId());

        eventPublisher.publishEvent(new ReviewDeletedEvent(
            new ReviewId(cmd.reviewId()),
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
                new ReviewId(cmd.reviewId()),
                cmd.memberId(),
                false,
                LocalDateTime.now()
            ));
            return false;
        } else {
            reviewLikeRepository.save(new ReviewLike(cmd.reviewId(), cmd.memberId()));
            eventPublisher.publishEvent(new ReviewLikedEvent(
                new ReviewId(cmd.reviewId()),
                cmd.memberId(),
                true,
                LocalDateTime.now()
            ));
            return true;
        }
    }

    public ReviewComment createComment(CreateReviewCommentCommand cmd) {
        return reviewCommentRepository.save(new ReviewComment(cmd.reviewId(), cmd.memberId(), cmd.content()));
    }

    public ReviewReply createReply(CreateReviewReplyCommand cmd) {
        return reviewReplyRepository.save(new ReviewReply(cmd.commentId(), cmd.memberId(), cmd.replyToMemberId(), cmd.content()));
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
                    .orElseGet(() -> tagRepository.save(new Tag(tagName)));
                return new ReviewTag(reviewId, tag.getId());
            })
            .toList();
        reviewTagRepository.saveAll(reviewTags);
        return tagNames;
    }
}
