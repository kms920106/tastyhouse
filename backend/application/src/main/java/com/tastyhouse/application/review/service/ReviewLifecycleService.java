package com.tastyhouse.application.review.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
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
import com.tastyhouse.application.review.port.out.write.ReviewImagePersistencePort;
import com.tastyhouse.application.review.port.out.write.ReviewLikePersistencePort;
import com.tastyhouse.application.review.port.out.write.ReviewPersistencePort;
import com.tastyhouse.application.review.port.out.write.ReviewTagPersistencePort;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shop.port.out.write.TagPersistencePort;

@Service
public class ReviewLifecycleService {

    private final ReviewPersistencePort reviewPersistencePort;
    private final ReviewImagePersistencePort reviewImagePersistencePort;
    private final ReviewTagPersistencePort reviewTagPersistencePort;
    private final ReviewLikePersistencePort reviewLikePersistencePort;
    private final TagPersistencePort tagPersistencePort;
    private final DomainEventPublisher domainEventPublisher;

    public ReviewLifecycleService(
        ReviewPersistencePort reviewPersistencePort,
        ReviewImagePersistencePort reviewImagePersistencePort,
        ReviewTagPersistencePort reviewTagPersistencePort,
        ReviewLikePersistencePort reviewLikePersistencePort,
        TagPersistencePort tagPersistencePort,
        DomainEventPublisher domainEventPublisher
    ) {
        this.reviewPersistencePort = reviewPersistencePort;
        this.reviewImagePersistencePort = reviewImagePersistencePort;
        this.reviewTagPersistencePort = reviewTagPersistencePort;
        this.reviewLikePersistencePort = reviewLikePersistencePort;
        this.tagPersistencePort = tagPersistencePort;
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
        if (orderId != null && reviewPersistencePort.existsByOrderIdAndProductId(orderId, productId)) {
            throw new BusinessException(ErrorCode.REVIEW_ALREADY_EXISTS);
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

        Review saved = reviewPersistencePort.save(review);

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
        Review review = reviewPersistencePort.findByIdAndMemberId(reviewId, memberId)
            .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_ACCESS_DENIED));

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

        Review saved = reviewPersistencePort.save(review);

        reviewImagePersistencePort.deleteByReviewId(reviewId);
        reviewTagPersistencePort.deleteByReviewId(reviewId);

        List<Long> savedFileIds = saveImages(reviewId, uploadedFileIds);
        List<String> savedTags = saveTags(reviewId, tags);

        return new ReviewRegistration(saved, savedFileIds, savedTags);
    }

    public void removeOwnedBy(ReviewId reviewId, MemberId memberId, ProductId productId) {
        reviewPersistencePort.findByIdAndMemberId(reviewId, memberId)
            .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_ACCESS_DENIED));

        deleteWithChildren(reviewId);

        domainEventPublisher.publish(new ReviewDeletedEvent(
            reviewId,
            memberId,
            productId,
            LocalDateTime.now()
        ));
    }

    public void remove(ReviewId reviewId) {
        Review review = reviewPersistencePort.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_NOT_FOUND));

        deleteWithChildren(reviewId);

        domainEventPublisher.publish(new ReviewDeletedEvent(
            reviewId,
            review.getMemberId(),
            review.getProductId(),
            LocalDateTime.now()
        ));
    }

    public boolean toggleLike(ReviewId reviewId, MemberId memberId) {
        boolean liked = !reviewLikePersistencePort.existsByReviewIdAndMemberId(reviewId, memberId);

        if (liked) {
            reviewLikePersistencePort.save(ReviewLike.of(reviewId, memberId));
        } else {
            reviewLikePersistencePort.deleteByReviewIdAndMemberId(reviewId, memberId);
        }

        return liked;
    }

    private void deleteWithChildren(ReviewId reviewId) {
        reviewImagePersistencePort.deleteByReviewId(reviewId);
        reviewTagPersistencePort.deleteByReviewId(reviewId);
        reviewPersistencePort.deleteById(reviewId);
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
        reviewImagePersistencePort.saveAll(images);

        return uploadedFileIds;
    }

    private List<String> saveTags(ReviewId reviewId, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return List.of();
        }

        List<ReviewTag> reviewTags = tagNames.stream()
            .map(tagName -> {
                Tag tag = tagPersistencePort.findByTagName(tagName)
                    .orElseGet(() -> tagPersistencePort.save(Tag.of(tagName)));
                return ReviewTag.of(reviewId, TagId.of(tag.getId()));
            })
            .toList();
        reviewTagPersistencePort.saveAll(reviewTags);

        return tagNames;
    }
}
