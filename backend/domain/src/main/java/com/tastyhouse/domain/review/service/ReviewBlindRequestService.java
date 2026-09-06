package com.tastyhouse.domain.review.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.review.event.ReviewBlindApprovedEvent;
import com.tastyhouse.domain.review.model.Review;
import com.tastyhouse.domain.review.model.ReviewBlindReason;
import com.tastyhouse.domain.review.model.ReviewBlindRequest;
import com.tastyhouse.domain.review.model.ReviewBlindRequestAttachment;
import com.tastyhouse.domain.review.model.ReviewBlindStatus;
import com.tastyhouse.domain.review.repository.ReviewBlindRequestAttachmentRepository;
import com.tastyhouse.domain.review.repository.ReviewBlindRequestRepository;
import com.tastyhouse.domain.review.repository.ReviewRepository;
import com.tastyhouse.domain.review.vo.ReviewBlindRequestId;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;
import com.tastyhouse.domain.shop.model.ShopRequestStatus;
import com.tastyhouse.domain.shop.model.ShopRequestType;
import com.tastyhouse.domain.shop.service.ShopRequestIndexRecorder;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ReviewBlindRequestService {
    private final ReviewBlindRequestRepository reviewBlindRequestRepository;
    private final ReviewBlindRequestAttachmentRepository reviewBlindRequestAttachmentRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewLifecycleService reviewLifecycleService;
    private final ShopRequestIndexRecorder shopRequestIndexRecorder;
    private final DomainEventPublisher domainEventPublisher;

    public ReviewBlindRequestService(
        ReviewBlindRequestRepository reviewBlindRequestRepository,
        ReviewBlindRequestAttachmentRepository reviewBlindRequestAttachmentRepository,
        ReviewRepository reviewRepository,
        ReviewLifecycleService reviewLifecycleService,
        ShopRequestIndexRecorder shopRequestIndexRecorder,
        DomainEventPublisher domainEventPublisher
    ) {
        this.reviewBlindRequestRepository = reviewBlindRequestRepository;
        this.reviewBlindRequestAttachmentRepository = reviewBlindRequestAttachmentRepository;
        this.reviewRepository = reviewRepository;
        this.reviewLifecycleService = reviewLifecycleService;
        this.shopRequestIndexRecorder = shopRequestIndexRecorder;
        this.domainEventPublisher = domainEventPublisher;
    }

    public Long request(
        Long shopId,
        Long reviewId,
        Long ceoId,
        ReviewBlindReason reason,
        String detailReason,
        List<Long> attachmentFileIds
    ) {
        ReviewId targetReviewId = ReviewId.of(reviewId);
        loadReviewOfShop(targetReviewId, shopId);
        validateDetailReason(reason, detailReason);

        if (reviewBlindRequestRepository.existsByReviewIdAndStatus(targetReviewId, ReviewBlindStatus.PENDING)) {
            throw new BusinessException(ErrorCode.REVIEW_BLIND_REQUEST_ALREADY_PENDING);
        }
        if (reviewBlindRequestRepository.existsTerminatedByReviewId(targetReviewId)) {
            throw new BusinessException(ErrorCode.REVIEW_BLIND_REQUEST_ALREADY_USED);
        }

        ReviewBlindRequest saved = reviewBlindRequestRepository.save(
            ReviewBlindRequest.of(targetReviewId, ShopId.of(shopId), CeoId.of(ceoId), reason, detailReason)
        );

        saveAttachments(saved.getId(), attachmentFileIds);

        shopRequestIndexRecorder.record(
            ShopId.of(shopId),
            ShopRequestType.REVIEW_BLIND,
            saved.getId(),
            describe(reason, reviewId),
            null,
            ceoId
        );
        return saved.getId();
    }

    public void approve(Long requestId, LocalDateTime now) {
        ReviewBlindRequest request = loadRequest(requestId);
        request.approve(now.plusDays(ReviewBlindRequest.BLIND_PERIOD_DAYS));
        request = reviewBlindRequestRepository.save(request);

        Review review = reviewRepository.findById(request.getReviewId())
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_NOT_FOUND));
        review.hide();
        reviewRepository.save(review);

        domainEventPublisher.publish(ReviewBlindApprovedEvent.of(
            request.getReviewId(),
            review.getMemberId(),
            ReviewBlindRequestId.of(request.getId()),
            request.getBlindUntil(),
            now
        ));

        shopRequestIndexRecorder.syncBlindRequestStatus(request.getId(), toShopRequestStatus(request.getStatus()), null);
    }

    public void reject(Long requestId, String rejectReason) {
        ReviewBlindRequest request = loadRequest(requestId);
        request.reject(rejectReason);
        request = reviewBlindRequestRepository.save(request);

        shopRequestIndexRecorder.syncBlindRequestStatus(request.getId(), toShopRequestStatus(request.getStatus()), rejectReason);
    }

    public void cancel(Long requestId, Long shopId) {
        ReviewBlindRequest request = loadRequest(requestId);
        if (!request.getShopId().equals(ShopId.of(shopId))) {
            throw new ResourceNotFoundException(ErrorCode.REVIEW_BLIND_REQUEST_NOT_FOUND);
        }
        request.cancel();
        request = reviewBlindRequestRepository.save(request);

        shopRequestIndexRecorder.syncCanceled(ShopRequestType.REVIEW_BLIND, request.getId());
    }

    public void consentToDelete(ReviewId reviewId, MemberId memberId) {
        Review review = loadOwnedReview(reviewId, memberId);

        ReviewBlindRequest request = reviewBlindRequestRepository.findApprovedByReviewId(reviewId)
            .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_BLIND_REQUEST_NOT_APPROVED));

        request.deleteByConsent();
        request = reviewBlindRequestRepository.save(request);

        reviewLifecycleService.removeOwnedBy(reviewId, memberId, review.getProductId());

        shopRequestIndexRecorder.syncBlindRequestStatus(request.getId(), toShopRequestStatus(request.getStatus()), null);
    }

    public void rejectDeletion(ReviewId reviewId, MemberId memberId) {
        loadOwnedReview(reviewId, memberId);

        reviewBlindRequestRepository.findApprovedByReviewId(reviewId)
            .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_BLIND_REQUEST_NOT_APPROVED));
    }

    public void expire(Long requestId) {
        ReviewBlindRequest request = loadRequest(requestId);
        request.expire();
        request = reviewBlindRequestRepository.save(request);

        reviewRepository.findById(request.getReviewId()).ifPresent(review -> {
            review.unhide();
            reviewRepository.save(review);
        });

        shopRequestIndexRecorder.syncBlindRequestStatus(request.getId(), toShopRequestStatus(request.getStatus()), null);
    }

    public List<ReviewBlindRequest> findExpirableBlinds(LocalDateTime now) {
        return reviewBlindRequestRepository.findExpirableBlinds(now);
    }

    private static ShopRequestStatus toShopRequestStatus(ReviewBlindStatus status) {
        return switch (status) {
            case PENDING -> ShopRequestStatus.PENDING;
            case APPROVED -> ShopRequestStatus.APPROVED;
            case REJECTED -> ShopRequestStatus.REJECTED;
            case CANCELED -> ShopRequestStatus.CANCELED;
            case EXPIRED -> ShopRequestStatus.APPROVED;
            case DELETED -> ShopRequestStatus.APPROVED;
        };
    }

    private void saveAttachments(Long blindRequestId, List<Long> attachmentFileIds) {
        if (attachmentFileIds == null || attachmentFileIds.isEmpty()) {
            return;
        }

        ReviewBlindRequestId requestId = ReviewBlindRequestId.of(blindRequestId);
        List<ReviewBlindRequestAttachment> attachments = new ArrayList<>();
        for (int i = 0; i < attachmentFileIds.size(); i++) {
            attachments.add(ReviewBlindRequestAttachment.of(
                requestId,
                UploadedFileId.of(attachmentFileIds.get(i)),
                i + 1
            ));
        }
        reviewBlindRequestAttachmentRepository.saveAll(attachments);
    }

    private void validateDetailReason(ReviewBlindReason reason, String detailReason) {
        if (reason == ReviewBlindReason.ETC && (detailReason == null || detailReason.isBlank())) {
            throw new BusinessException(ErrorCode.REVIEW_BLIND_DETAIL_REASON_REQUIRED);
        }
    }

    private String describe(ReviewBlindReason reason, Long reviewId) {
        return ShopRequestType.REVIEW_BLIND.getDescription() + " - " + reason.getDescription()
            + "(리뷰 #" + reviewId + ")";
    }

    private ReviewBlindRequest loadRequest(Long requestId) {
        return reviewBlindRequestRepository.findById(ReviewBlindRequestId.of(requestId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_BLIND_REQUEST_NOT_FOUND));
    }

    private void loadReviewOfShop(ReviewId reviewId, Long shopId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_NOT_FOUND));
        if (!review.getShopId().equals(ShopId.of(shopId))) {
            throw new BusinessException(ErrorCode.SHOP_ACCESS_DENIED);
        }
    }

    private Review loadOwnedReview(ReviewId reviewId, MemberId memberId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_NOT_FOUND));
        if (!review.getMemberId().equals(memberId)) {
            throw new ResourceNotFoundException(ErrorCode.REVIEW_NOT_FOUND);
        }
        return review;
    }
}
