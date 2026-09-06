package com.tastyhouse.application.review.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.review.port.in.ReviewOwnerReplyCommandUseCase;
import com.tastyhouse.application.review.port.in.ReviewOwnerReplyCreateCommand;
import com.tastyhouse.application.review.port.in.ReviewOwnerReplyDeleteCommand;
import com.tastyhouse.application.review.port.in.ReviewOwnerReplyUpdateCommand;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;
import com.tastyhouse.domain.review.service.ReviewOwnerReplyService;

@Service
@CeoApp
@Transactional
public class ReviewOwnerReplyCommandService implements ReviewOwnerReplyCommandUseCase {

    private final ReviewOwnerReplyService reviewOwnerReplyService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ReviewOwnerReplyCommandService(
        ReviewOwnerReplyService reviewOwnerReplyService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.reviewOwnerReplyService = reviewOwnerReplyService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public Long register(ReviewOwnerReplyCreateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return reviewOwnerReplyService.register(shopId, command.reviewId(), ceoId, command.content(), LocalDate.now());
    }

    @Override
    public void modify(ReviewOwnerReplyUpdateCommand command) {
        Long shopId = command.shopId();

        shopOwnershipValidator.validateOwnership(command.ceoId(), shopId);
        reviewOwnerReplyService.modify(shopId, command.reviewId(), command.content());
    }

    @Override
    public void remove(ReviewOwnerReplyDeleteCommand command) {
        Long shopId = command.shopId();

        shopOwnershipValidator.validateOwnership(command.ceoId(), shopId);
        reviewOwnerReplyService.remove(shopId, command.reviewId());
    }
}
