package com.tastyhouse.application.product.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.vo.StorePriceVerificationId;
import com.tastyhouse.application.product.port.in.StorePriceVerificationRejectCommand;
import com.tastyhouse.application.product.port.in.StorePriceVerificationRejectUseCase;

@Service
@Transactional
class StorePriceVerificationRejectService implements StorePriceVerificationRejectUseCase {

    private final StorePriceVerificationService storePriceVerificationService;

    public StorePriceVerificationRejectService(StorePriceVerificationService storePriceVerificationService) {
        this.storePriceVerificationService = storePriceVerificationService;
    }

    @Override
    public void reject(StorePriceVerificationRejectCommand command) {
        Long id = command.verificationId();
        String rejectReason = command.rejectReason();
        StorePriceVerificationId verificationId = StorePriceVerificationId.of(id);
        storePriceVerificationService.reject(verificationId, rejectReason, LocalDateTime.now());
    }
}
