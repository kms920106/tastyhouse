package com.tastyhouse.application.product.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.vo.StorePriceVerificationId;
import com.tastyhouse.application.product.port.in.StorePriceVerificationStartReviewCommand;
import com.tastyhouse.application.product.port.in.StorePriceVerificationStartReviewUseCase;

@Service
@Transactional
class StorePriceVerificationStartReviewService implements StorePriceVerificationStartReviewUseCase {

    private final StorePriceVerificationService storePriceVerificationService;

    public StorePriceVerificationStartReviewService(StorePriceVerificationService storePriceVerificationService) {
        this.storePriceVerificationService = storePriceVerificationService;
    }

    @Override
    public void startReview(StorePriceVerificationStartReviewCommand command) {
        Long id = command.verificationId();
        StorePriceVerificationId verificationId = StorePriceVerificationId.of(id);
        storePriceVerificationService.startReview(verificationId, LocalDateTime.now());
    }
}
