package com.tastyhouse.application.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.payment.port.in.PaymentOnSiteCompleteCommand;
import com.tastyhouse.application.payment.port.in.PaymentOnSiteCompleteUseCase;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class PaymentOnSiteCompleteService implements PaymentOnSiteCompleteUseCase {

    private final ShopOwnershipValidator shopOwnershipValidator;
    private final PaymentConfirmationService paymentConfirmationService;

    public PaymentOnSiteCompleteService(
        ShopOwnershipValidator shopOwnershipValidator,
        PaymentConfirmationService paymentConfirmationService
    ) {
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.paymentConfirmationService = paymentConfirmationService;
    }

    @Override
    public Long completeOnSitePayment(PaymentOnSiteCompleteCommand command) {
        shopOwnershipValidator.validateOwnership(command.ceoId(), command.shopId());

        ShopId shopId = ShopId.of(command.shopId());
        PaymentId paymentId = PaymentId.of(command.paymentId());
        return paymentConfirmationService.completeOnSitePayment(shopId, paymentId).value();
    }
}
