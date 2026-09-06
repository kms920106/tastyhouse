package com.tastyhouse.application.review.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.review.port.in.ReviewBlindRequestCancelCommand;
import com.tastyhouse.application.review.port.in.ReviewBlindRequestOwnerCommandUseCase;
import com.tastyhouse.application.review.port.in.ReviewBlindRequestCreateCommand;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;
import com.tastyhouse.domain.review.model.ReviewBlindReason;
import com.tastyhouse.domain.review.service.ReviewBlindRequestService;

@Service
@CeoApp
@Transactional
public class ReviewBlindRequestOwnerCommandService implements ReviewBlindRequestOwnerCommandUseCase {

    private final ReviewBlindRequestService reviewBlindRequestService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ReviewBlindRequestOwnerCommandService(
        ReviewBlindRequestService reviewBlindRequestService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.reviewBlindRequestService = reviewBlindRequestService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public Long request(ReviewBlindRequestCreateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long reviewId = command.reviewId();
        String detailReason = command.detailReason();
        List<Long> attachmentFileIds = command.attachmentFileIds();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        ReviewBlindReason blindReason = ReviewBlindReason.from(command.reason());
        return reviewBlindRequestService.request(shopId, reviewId, ceoId, blindReason, detailReason, attachmentFileIds);
    }

    @Override
    public void cancel(ReviewBlindRequestCancelCommand command) {
        Long shopId = command.shopId();

        shopOwnershipValidator.validateOwnership(command.ceoId(), shopId);
        reviewBlindRequestService.cancel(command.blindRequestId(), shopId);
    }
}
