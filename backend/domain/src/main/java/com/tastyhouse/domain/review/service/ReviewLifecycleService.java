package com.tastyhouse.domain.review.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.review.event.ReviewCreatedEvent;
import com.tastyhouse.domain.review.event.ReviewDeletedEvent;
import com.tastyhouse.domain.review.model.Review;
import com.tastyhouse.domain.review.model.ReviewImage;
import com.tastyhouse.domain.review.model.ReviewLike;
import com.tastyhouse.domain.review.model.ReviewTag;
import com.tastyhouse.domain.review.repository.ReviewImageRepository;
import com.tastyhouse.domain.review.repository.ReviewLikeRepository;
import com.tastyhouse.domain.review.repository.ReviewRepository;
import com.tastyhouse.domain.review.repository.ReviewTagRepository;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shop.model.Tag;
import com.tastyhouse.domain.shop.repository.TagRepository;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shop.vo.TagId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;

public class ReviewLifecycleService {
    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final ReviewTagRepository reviewTagRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final TagRepository tagRepository;
    private final DomainEventPublisher domainEventPublisher;

    public ReviewLifecycleService(
        ReviewRepository reviewRepository,
        ReviewImageRepository reviewImageRepository,
        ReviewTagRepository reviewTagRepository,
        ReviewLikeRepository reviewLikeRepository,
        TagRepository tagRepository,
        DomainEventPublisher domainEventPublisher
    ) {
        this.reviewRepository = reviewRepository;
        this.reviewImageRepository = reviewImageRepository;
        this.reviewTagRepository = reviewTagRepository;
        this.reviewLikeRepository = reviewLikeRepository;
        this.tagRepository = tagRepository;
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
        if (orderId != null && reviewRepository.existsByOrderIdAndProductId(orderId, productId)) {
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

        Review saved = reviewRepository.save(review);

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
        Review review = reviewRepository.findByIdAndMemberId(reviewId, memberId)
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

        Review saved = reviewRepository.save(review);

        reviewImageRepository.deleteByReviewId(reviewId);
        reviewTagRepository.deleteByReviewId(reviewId);

        List<Long> savedFileIds = saveImages(reviewId, uploadedFileIds);
        List<String> savedTags = saveTags(reviewId, tags);

        return new ReviewRegistration(saved, savedFileIds, savedTags);
    }

    public void removeOwnedBy(ReviewId reviewId, MemberId memberId, ProductId productId) {
        reviewRepository.findByIdAndMemberId(reviewId, memberId)
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
        Review review = reviewRepository.findById(reviewId)
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
        boolean liked = !reviewLikeRepository.existsByReviewIdAndMemberId(reviewId, memberId);

        if (liked) {
            reviewLikeRepository.save(ReviewLike.of(reviewId, memberId));
        } else {
            reviewLikeRepository.deleteByReviewIdAndMemberId(reviewId, memberId);
        }

        return liked;
    }

    private void deleteWithChildren(ReviewId reviewId) {
        reviewImageRepository.deleteByReviewId(reviewId);
        reviewTagRepository.deleteByReviewId(reviewId);
        reviewRepository.deleteById(reviewId);
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
        reviewImageRepository.saveAll(images);

        return uploadedFileIds;
    }

    private List<String> saveTags(ReviewId reviewId, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return List.of();
        }

        List<ReviewTag> reviewTags = tagNames.stream()
            .map(tagName -> {
                Tag tag = tagRepository.findByTagName(tagName)
                    .orElseGet(() -> tagRepository.save(Tag.of(tagName)));
                return ReviewTag.of(reviewId, TagId.of(tag.getId()));
            })
            .toList();
        reviewTagRepository.saveAll(reviewTags);

        return tagNames;
    }
}
