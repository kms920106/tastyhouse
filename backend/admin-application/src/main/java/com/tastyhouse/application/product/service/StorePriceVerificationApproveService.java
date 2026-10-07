package com.tastyhouse.application.product.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.vo.StorePriceVerificationId;
import com.tastyhouse.application.product.port.in.StorePriceVerificationApproveCommand;
import com.tastyhouse.application.product.port.in.StorePriceVerificationApproveUseCase;

@Service
@Transactional
class StorePriceVerificationApproveService implements StorePriceVerificationApproveUseCase {

    private final StorePriceVerificationService storePriceVerificationService;

    public StorePriceVerificationApproveService(StorePriceVerificationService storePriceVerificationService) {
        this.storePriceVerificationService = storePriceVerificationService;
    }

    @Override
    public void approve(StorePriceVerificationApproveCommand command) {
        Long id = command.verificationId();
        StorePriceVerificationId verificationId = StorePriceVerificationId.of(id);
        storePriceVerificationService.approve(verificationId, LocalDateTime.now());
    }
}
