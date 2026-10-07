package com.tastyhouse.application.review.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.review.port.in.ReviewOwnerReplyCreateCommand;
import com.tastyhouse.application.review.port.in.ReviewOwnerReplyCreateUseCase;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ReviewOwnerReplyCreateService implements ReviewOwnerReplyCreateUseCase {

    private final ReviewOwnerReplyService reviewOwnerReplyService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ReviewOwnerReplyCreateService(
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
}
