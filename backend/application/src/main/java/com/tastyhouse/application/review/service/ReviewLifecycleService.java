package com.tastyhouse.application.review.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.review.event.ReviewCreatedEvent;
import com.tastyhouse.domain.review.event.ReviewDeletedEvent;
import com.tastyhouse.domain.review.model.Review;
import com.tastyhouse.domain.review.model.ReviewImage;
import com.tastyhouse.domain.review.model.ReviewLike;
import com.tastyhouse.domain.review.model.ReviewRegistration;
import com.tastyhouse.domain.review.model.ReviewTag;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shop.model.Tag;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shop.vo.TagId;
import com.tastyhouse.application.review.port.out.write.ReviewImageSavePort;
import com.tastyhouse.application.review.port.out.write.ReviewLikeLoadPort;
import com.tastyhouse.application.review.port.out.write.ReviewLikeSavePort;
import com.tastyhouse.application.review.port.out.write.ReviewLoadPort;
import com.tastyhouse.application.review.port.out.write.ReviewSavePort;
import com.tastyhouse.application.review.port.out.write.ReviewTagSavePort;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.out.write.TagLoadPort;
import com.tastyhouse.application.shop.port.out.write.TagSavePort;

@Service
public class ReviewLifecycleService {

    private final ReviewLoadPort reviewLoadPort;
    private final ReviewSavePort reviewSavePort;
    private final ReviewImageSavePort reviewImageSavePort;
    private final ReviewTagSavePort reviewTagSavePort;
    private final ReviewLikeLoadPort reviewLikeLoadPort;
    private final ReviewLikeSavePort reviewLikeSavePort;
    private final TagLoadPort tagLoadPort;
    private final TagSavePort tagSavePort;
    private final DomainEventPublisher domainEventPublisher;

    public ReviewLifecycleService(
        ReviewLoadPort reviewLoadPort,
        ReviewSavePort reviewSavePort,
        ReviewImageSavePort reviewImageSavePort,
        ReviewTagSavePort reviewTagSavePort,
        ReviewLikeLoadPort reviewLikeLoadPort,
        ReviewLikeSavePort reviewLikeSavePort,
        TagLoadPort tagLoadPort,
        TagSavePort tagSavePort,
        DomainEventPublisher domainEventPublisher
    ) {
        this.reviewLoadPort = reviewLoadPort;
        this.reviewSavePort = reviewSavePort;
        this.reviewImageSavePort = reviewImageSavePort;
        this.reviewTagSavePort = reviewTagSavePort;
        this.reviewLikeLoadPort = reviewLikeLoadPort;
        this.reviewLikeSavePort = reviewLikeSavePort;
        this.tagLoadPort = tagLoadPort;
        this.tagSavePort = tagSavePort;
        this.domainEventPublisher = domainEventPublisher;
    }

    public ReviewRegistration register(
        ShopId shopId,
        ProductId productId,
        MemberId memberId,
        OrderId orderId,
        Integer tasteRating,
        Integer amountRating,
        Integer priceRating,
        String content,
        List<Long> uploadedFileIds,
        List<String> tags,
        boolean ownerOnly,
        Integer deliveryRating,
        String deliveryComment
    ) {
        if (orderId != null && reviewLoadPort.existsByOrderIdAndProductId(orderId, productId)) {
            throw new ApplicationException(ApplicationErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Review review = Review.of(
            shopId,
            productId,
            memberId,
            content,
            averageRating(tasteRating, amountRating, priceRating),
            tasteRating.doubleValue(),
            amountRating.doubleValue(),
            priceRating.doubleValue(),
            null, null, null, false,
            orderId,
            ownerOnly,
            deliveryRating,
            deliveryComment
        );

        Review saved = reviewSavePort.save(review);

        List<Long> savedFileIds = saveImages(saved.getReviewId(), uploadedFileIds);
        List<String> savedTags = saveTags(saved.getReviewId(), tags);

        domainEventPublisher.publish(new ReviewCreatedEvent(
            saved.getReviewId(),
            memberId,
            shopId,
            productId,
            LocalDateTime.now()
        ));

        return new ReviewRegistration(saved, savedFileIds, savedTags);
    }

    public ReviewRegistration modify(
        ReviewId reviewId,
        MemberId memberId,
        Integer tasteRating,
        Integer amountRating,
        Integer priceRating,
        String content,
        List<Long> uploadedFileIds,
        List<String> tags,
        Integer deliveryRating,
        String deliveryComment
    ) {
        Review review = reviewLoadPort.findByIdAndMemberId(reviewId, memberId)
            .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.REVIEW_ACCESS_DENIED));

        review.updateContent(
            content,
            averageRating(tasteRating, amountRating, priceRating),
            tasteRating.doubleValue(),
            amountRating.doubleValue(),
            priceRating.doubleValue(),
            null, null, null, false,
            deliveryRating,
            deliveryComment
        );

        Review saved = reviewSavePort.save(review);

        reviewImageSavePort.deleteByReviewId(reviewId);
        reviewTagSavePort.deleteByReviewId(reviewId);

        List<Long> savedFileIds = saveImages(reviewId, uploadedFileIds);
        List<String> savedTags = saveTags(reviewId, tags);

        return new ReviewRegistration(saved, savedFileIds, savedTags);
    }

    public void removeOwnedBy(ReviewId reviewId, MemberId memberId, ProductId productId) {
        reviewLoadPort.findByIdAndMemberId(reviewId, memberId)
            .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.REVIEW_ACCESS_DENIED));

        deleteWithChildren(reviewId);

        domainEventPublisher.publish(new ReviewDeletedEvent(
            reviewId,
            memberId,
            productId,
            LocalDateTime.now()
        ));
    }

    public void remove(ReviewId reviewId) {
        Review review = reviewLoadPort.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.REVIEW_NOT_FOUND));

        deleteWithChildren(reviewId);

        domainEventPublisher.publish(new ReviewDeletedEvent(
            reviewId,
            review.getMemberId(),
            review.getProductId(),
            LocalDateTime.now()
        ));
    }

    public boolean toggleLike(ReviewId reviewId, MemberId memberId) {
        boolean liked = !reviewLikeLoadPort.existsByReviewIdAndMemberId(reviewId, memberId);

        if (liked) {
            reviewLikeSavePort.save(ReviewLike.of(reviewId, memberId));
        } else {
            reviewLikeSavePort.deleteByReviewIdAndMemberId(reviewId, memberId);
        }

        return liked;
    }

    private void deleteWithChildren(ReviewId reviewId) {
        reviewImageSavePort.deleteByReviewId(reviewId);
        reviewTagSavePort.deleteByReviewId(reviewId);
        reviewSavePort.deleteById(reviewId);
    }

    private double averageRating(Integer tasteRating, Integer amountRating, Integer priceRating) {
        return Math.round((tasteRating + amountRating + priceRating) / 3.0 * 10.0) / 10.0;
    }

    private List<Long> saveImages(ReviewId reviewId, List<Long> uploadedFileIds) {
        if (uploadedFileIds == null || uploadedFileIds.isEmpty()) {
            return List.of();
        }

        List<ReviewImage> images = new ArrayList<>();
        for (int i = 0; i < uploadedFileIds.size(); i++) {
            images.add(ReviewImage.of(reviewId, UploadedFileId.of(uploadedFileIds.get(i)), i + 1));
        }
        reviewImageSavePort.saveAll(images);

        return uploadedFileIds;
    }

    private List<String> saveTags(ReviewId reviewId, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return List.of();
        }

        List<ReviewTag> reviewTags = tagNames.stream()
            .map(tagName -> {
                Tag tag = tagLoadPort.findByTagName(tagName)
                    .orElseGet(() -> tagSavePort.save(Tag.of(tagName)));
                return ReviewTag.of(reviewId, TagId.of(tag.getId()));
            })
            .toList();
        reviewTagSavePort.saveAll(reviewTags);

        return tagNames;
    }
}
