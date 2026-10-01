package com.tastyhouse.application.review.service;

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
import com.tastyhouse.domain.review.vo.ReviewBlindRequestId;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shop.model.ShopRequestStatus;
import com.tastyhouse.domain.shop.model.ShopRequestType;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.review.port.out.write.ReviewBlindRequestAttachmentPersistencePort;
import com.tastyhouse.application.review.port.out.write.ReviewBlindRequestPersistencePort;
import com.tastyhouse.application.review.port.out.write.ReviewPersistencePort;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.marker.SharedApp;
import com.tastyhouse.application.shop.service.ShopRequestIndexRecorder;

@SharedApp
public class ReviewBlindRequestService {

    private final ReviewBlindRequestPersistencePort reviewBlindRequestPersistencePort;
    private final ReviewBlindRequestAttachmentPersistencePort reviewBlindRequestAttachmentPersistencePort;
    private final ReviewPersistencePort reviewPersistencePort;
    private final ReviewLifecycleService reviewLifecycleService;
    private final ShopRequestIndexRecorder shopRequestIndexRecorder;
    private final DomainEventPublisher domainEventPublisher;

    public ReviewBlindRequestService(
        ReviewBlindRequestPersistencePort reviewBlindRequestPersistencePort,
        ReviewBlindRequestAttachmentPersistencePort reviewBlindRequestAttachmentPersistencePort,
        ReviewPersistencePort reviewPersistencePort,
        ReviewLifecycleService reviewLifecycleService,
        ShopRequestIndexRecorder shopRequestIndexRecorder,
        DomainEventPublisher domainEventPublisher
    ) {
        this.reviewBlindRequestPersistencePort = reviewBlindRequestPersistencePort;
        this.reviewBlindRequestAttachmentPersistencePort = reviewBlindRequestAttachmentPersistencePort;
        this.reviewPersistencePort = reviewPersistencePort;
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

        if (reviewBlindRequestPersistencePort.existsByReviewIdAndStatus(targetReviewId, ReviewBlindStatus.PENDING)) {
            throw new BusinessException(ErrorCode.REVIEW_BLIND_REQUEST_ALREADY_PENDING);
        }
        if (reviewBlindRequestPersistencePort.existsTerminatedByReviewId(targetReviewId)) {
            throw new BusinessException(ErrorCode.REVIEW_BLIND_REQUEST_ALREADY_USED);
        }

        ReviewBlindRequest saved = reviewBlindRequestPersistencePort.save(
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
        request = reviewBlindRequestPersistencePort.save(request);

        Review review = reviewPersistencePort.findById(request.getReviewId())
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_NOT_FOUND));
        review.hide();
        reviewPersistencePort.save(review);

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
        request = reviewBlindRequestPersistencePort.save(request);

        shopRequestIndexRecorder.syncBlindRequestStatus(request.getId(), toShopRequestStatus(request.getStatus()), rejectReason);
    }

    public void cancel(Long requestId, Long shopId) {
        ReviewBlindRequest request = loadRequest(requestId);
        if (!request.getShopId().equals(ShopId.of(shopId))) {
            throw new ResourceNotFoundException(ErrorCode.REVIEW_BLIND_REQUEST_NOT_FOUND);
        }
        request.cancel();
        request = reviewBlindRequestPersistencePort.save(request);

        shopRequestIndexRecorder.syncCanceled(ShopRequestType.REVIEW_BLIND, request.getId());
    }

    public void consentToDelete(ReviewId reviewId, MemberId memberId) {
        Review review = loadOwnedReview(reviewId, memberId);

        ReviewBlindRequest request = reviewBlindRequestPersistencePort.findApprovedByReviewId(reviewId)
            .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_BLIND_REQUEST_NOT_APPROVED));

        request.deleteByConsent();
        request = reviewBlindRequestPersistencePort.save(request);

        reviewLifecycleService.removeOwnedBy(reviewId, memberId, review.getProductId());

        shopRequestIndexRecorder.syncBlindRequestStatus(request.getId(), toShopRequestStatus(request.getStatus()), null);
    }

    public void rejectDeletion(ReviewId reviewId, MemberId memberId) {
        loadOwnedReview(reviewId, memberId);

        reviewBlindRequestPersistencePort.findApprovedByReviewId(reviewId)
            .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_BLIND_REQUEST_NOT_APPROVED));
    }

    public void expire(Long requestId) {
        ReviewBlindRequest request = loadRequest(requestId);
        request.expire();
        request = reviewBlindRequestPersistencePort.save(request);

        reviewPersistencePort.findById(request.getReviewId()).ifPresent(review -> {
            review.unhide();
            reviewPersistencePort.save(review);
        });

        shopRequestIndexRecorder.syncBlindRequestStatus(request.getId(), toShopRequestStatus(request.getStatus()), null);
    }

    public List<ReviewBlindRequest> findExpirableBlinds(LocalDateTime now) {
        return reviewBlindRequestPersistencePort.findExpirableBlinds(now);
    }

    public static ShopRequestStatus toShopRequestStatus(ReviewBlindStatus status) {
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
        reviewBlindRequestAttachmentPersistencePort.saveAll(attachments);
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
        return reviewBlindRequestPersistencePort.findById(ReviewBlindRequestId.of(requestId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_BLIND_REQUEST_NOT_FOUND));
    }

    private void loadReviewOfShop(ReviewId reviewId, Long shopId) {
        Review review = reviewPersistencePort.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_NOT_FOUND));
        if (!review.getShopId().equals(ShopId.of(shopId))) {
            throw new BusinessException(ErrorCode.SHOP_ACCESS_DENIED);
        }
    }

    private Review loadOwnedReview(ReviewId reviewId, MemberId memberId) {
        Review review = reviewPersistencePort.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_NOT_FOUND));
        if (!review.getMemberId().equals(memberId)) {
            throw new ResourceNotFoundException(ErrorCode.REVIEW_NOT_FOUND);
        }
        return review;
    }
}
