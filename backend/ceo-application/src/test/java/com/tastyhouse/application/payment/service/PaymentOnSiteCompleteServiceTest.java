package com.tastyhouse.application.payment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.payment.port.in.PaymentOnSiteCompleteCommand;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PaymentOnSiteCompleteServiceTest {

    private static final Long CEO_ID = 7L;
    private static final Long SHOP_ID = 10L;
    private static final Long PAYMENT_ID = 200L;

    private ShopOwnershipValidator shopOwnershipValidator;
    private PaymentConfirmationService paymentConfirmationService;
    private PaymentOnSiteCompleteService service;

    @BeforeEach
    void setUp() {
        shopOwnershipValidator = mock(ShopOwnershipValidator.class);
        paymentConfirmationService = mock(PaymentConfirmationService.class);
        service = new PaymentOnSiteCompleteService(shopOwnershipValidator, paymentConfirmationService);
    }

    @Test
    @DisplayName("소유한 가게가 아니면 가게 접근 거부로 막고 결제 완료 처리를 호출하지 않는다")
    void rejectsShopNotOwnedByCeo() {
        when(shopOwnershipValidator.validateOwnership(CEO_ID, SHOP_ID))
            .thenThrow(new ApplicationException(ApplicationErrorCode.SHOP_ACCESS_DENIED));

        assertThatThrownBy(() -> service.completeOnSitePayment(PaymentOnSiteCompleteCommand.of(CEO_ID, SHOP_ID, PAYMENT_ID)))
            .isInstanceOf(ApplicationException.class)
            .hasMessageContaining(ApplicationErrorCode.SHOP_ACCESS_DENIED.getDefaultMessage());

        verifyNoInteractions(paymentConfirmationService);
    }

    @Test
    @DisplayName("소유한 가게면 그 가게 기준으로 현장결제를 완료 처리한다")
    void completesOnSitePaymentForOwnedShop() {
        when(paymentConfirmationService.completeOnSitePayment(ShopId.of(SHOP_ID), PaymentId.of(PAYMENT_ID)))
            .thenReturn(PaymentId.of(PAYMENT_ID));

        Long completed = service.completeOnSitePayment(PaymentOnSiteCompleteCommand.of(CEO_ID, SHOP_ID, PAYMENT_ID));

        assertThat(completed).isEqualTo(PAYMENT_ID);
        verify(shopOwnershipValidator).validateOwnership(CEO_ID, SHOP_ID);
        verify(paymentConfirmationService).completeOnSitePayment(ShopId.of(SHOP_ID), PaymentId.of(PAYMENT_ID));
    }
}
