package com.tastyhouse.application.product.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.StorePriceVerificationApproveCommand;
import com.tastyhouse.application.product.port.in.StorePriceVerificationCommandUseCase;
import com.tastyhouse.application.product.port.in.StorePriceVerificationRejectCommand;
import com.tastyhouse.application.product.port.in.StorePriceVerificationStartReviewCommand;
import com.tastyhouse.domain.product.service.StorePriceVerificationService;
import com.tastyhouse.domain.product.vo.StorePriceVerificationId;

@Service
@AdminApp
@Transactional
public class StorePriceVerificationCommandService implements StorePriceVerificationCommandUseCase {

    private final StorePriceVerificationService storePriceVerificationService;

    public StorePriceVerificationCommandService(StorePriceVerificationService storePriceVerificationService) {
        this.storePriceVerificationService = storePriceVerificationService;
    }

    @Override
    public void startReview(StorePriceVerificationStartReviewCommand command) {
        Long id = command.verificationId();
        StorePriceVerificationId verificationId = StorePriceVerificationId.of(id);
        storePriceVerificationService.startReview(verificationId, LocalDateTime.now());
    }

    @Override
    public void approve(StorePriceVerificationApproveCommand command) {
        Long id = command.verificationId();
        StorePriceVerificationId verificationId = StorePriceVerificationId.of(id);
        storePriceVerificationService.approve(verificationId, LocalDateTime.now());
    }

    @Override
    public void reject(StorePriceVerificationRejectCommand command) {
        Long id = command.verificationId();
        String rejectReason = command.rejectReason();
        StorePriceVerificationId verificationId = StorePriceVerificationId.of(id);
        storePriceVerificationService.reject(verificationId, rejectReason, LocalDateTime.now());
    }
}
