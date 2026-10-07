package com.tastyhouse.application.product.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.product.model.ProductFeedbackType;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.application.product.port.in.ProductFeedbackCreateCommand;
import com.tastyhouse.application.product.port.in.ProductFeedbackCreateUseCase;

@Service
@Transactional
class ProductFeedbackCreateService implements ProductFeedbackCreateUseCase {

    private final ProductFeedbackService productFeedbackService;

    public ProductFeedbackCreateService(ProductFeedbackService productFeedbackService) {
        this.productFeedbackService = productFeedbackService;
    }

    @Override
    public Long submitFeedback(ProductFeedbackCreateCommand command) {
        MemberId reporterId = MemberId.of(command.memberId());
        ProductId targetProductId = ProductId.of(command.productId());
        ProductFeedbackType type = ProductFeedbackType.from(command.feedbackType());

        return productFeedbackService.submit(reporterId, targetProductId, type, command.content(), LocalDateTime.now())
            .getId();
    }
}
