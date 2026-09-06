package com.tastyhouse.application.product.service;

import com.tastyhouse.application.shared.marker.WebApp;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.product.model.ProductFeedbackType;
import com.tastyhouse.domain.product.service.ProductFeedbackService;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.application.product.port.in.ProductFeedbackCommandUseCase;
import com.tastyhouse.application.product.port.in.ProductFeedbackCreateCommand;

@Service
@WebApp
@Transactional
public class ProductFeedbackCommandService implements ProductFeedbackCommandUseCase {

    private final ProductFeedbackService productFeedbackService;

    public ProductFeedbackCommandService(ProductFeedbackService productFeedbackService) {
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
