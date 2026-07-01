package com.tastyhouse.external.payment.toss;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.tastyhouse.core.domain.payment.application.port.PgPaymentGateway;
import com.tastyhouse.core.domain.payment.application.port.dto.PgCancelResult;
import com.tastyhouse.core.domain.payment.application.port.dto.PgConfirmResult;
import com.tastyhouse.external.payment.toss.dto.TossPaymentConfirmResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class TossPaymentGatewayAdapter implements PgPaymentGateway {

    private final TossPaymentClient tossPaymentClient;

    @Override
    public PgConfirmResult confirmPayment(Long paymentId, String paymentKey, String pgOrderId, int amount) {
        TossPaymentConfirmResponse response = tossPaymentClient.confirmPayment(paymentKey, pgOrderId, amount);

        PgConfirmResult.TossPaymentDetail detail = buildTossPaymentDetail(response);

        if (response.isError()) {
            log.error("Toss confirm failed. pgOrderId: {}, errorCode: {}, msg: {}",
                pgOrderId, response.getCode(), response.getMessage());
            return new PgConfirmResult(
                false, null, null, null, null, null, null, null, null,
                response.getCode(), response.getMessage(), detail
            );
        }

        String cardCompany = null;
        String cardNumber = null;
        Integer installmentPlanMonths = null;

        if (response.getCard() != null) {
            TossPaymentConfirmResponse.Card card = response.getCard();
            cardCompany = TossPaymentUtils.mapIssuerCodeToCardCompany(card.getIssuerCode());
            cardNumber = card.getNumber();
            installmentPlanMonths = card.getInstallmentPlanMonths();
        }

        String receiptUrl = response.getReceipt() != null ? response.getReceipt().getUrl() : null;

        return new PgConfirmResult(
            true,
            response.getPaymentKey(),
            response.getStatus(),
            response.getTotalAmount(),
            TossPaymentUtils.parseDateTime(response.getApprovedAt()),
            receiptUrl,
            cardCompany,
            cardNumber,
            installmentPlanMonths,
            null,
            null,
            detail
        );
    }

    @Override
    public PgCancelResult cancelPayment(String pgTid, String cancelReason) {
        TossPaymentConfirmResponse response = tossPaymentClient.cancelPayment(pgTid, cancelReason);

        if (response.isError()) {
            log.error("Toss cancel failed. pgTid: {}, errorCode: {}, msg: {}",
                pgTid, response.getCode(), response.getMessage());
            return new PgCancelResult(false, response.getCode(), response.getMessage());
        }

        return new PgCancelResult(true, null, null);
    }

    private PgConfirmResult.TossPaymentDetail buildTossPaymentDetail(TossPaymentConfirmResponse response) {
        Integer cardAmount = null;
        String cardIssuerCode = null;
        String cardAcquirerCode = null;
        String cardNumber = null;
        Integer cardInstallmentPlanMonths = null;
        String cardApproveNo = null;
        boolean cardUseCardPoint = false;
        String cardType = null;
        String cardOwnerType = null;
        String cardAcquireStatus = null;
        boolean cardInterestFree = false;
        String cardInterestPayer = null;
        if (response.getCard() != null) {
            TossPaymentConfirmResponse.Card card = response.getCard();
            cardAmount = card.getAmount();
            cardIssuerCode = card.getIssuerCode();
            cardAcquirerCode = card.getAcquirerCode();
            cardNumber = card.getNumber();
            cardInstallmentPlanMonths = card.getInstallmentPlanMonths();
            cardApproveNo = card.getApproveNo();
            cardUseCardPoint = card.isUseCardPoint();
            cardType = card.getCardType();
            cardOwnerType = card.getOwnerType();
            cardAcquireStatus = card.getAcquireStatus();
            cardInterestFree = card.isInterestFree();
            cardInterestPayer = card.getInterestPayer();
        }

        String virtualAccountType = null;
        String virtualAccountNumber = null;
        String virtualAccountBankCode = null;
        String virtualAccountCustomerName = null;
        java.time.LocalDateTime virtualAccountDueDate = null;
        String virtualAccountRefundStatus = null;
        boolean virtualAccountExpired = false;
        String virtualAccountSettlementStatus = null;
        if (response.getVirtualAccount() != null) {
            TossPaymentConfirmResponse.VirtualAccount va = response.getVirtualAccount();
            virtualAccountType = va.getAccountType();
            virtualAccountNumber = va.getAccountNumber();
            virtualAccountBankCode = va.getBankCode();
            virtualAccountCustomerName = va.getCustomerName();
            virtualAccountDueDate = TossPaymentUtils.parseDateTime(va.getDueDate());
            virtualAccountRefundStatus = va.getRefundStatus();
            virtualAccountExpired = va.isExpired();
            virtualAccountSettlementStatus = va.getSettlementStatus();
        }

        String mobilePhoneCustomerMobilePhone = null;
        String mobilePhoneSettlementStatus = null;
        String mobilePhoneReceiptUrl = null;
        if (response.getMobilePhone() != null) {
            TossPaymentConfirmResponse.MobilePhone mp = response.getMobilePhone();
            mobilePhoneCustomerMobilePhone = mp.getCustomerMobilePhone();
            mobilePhoneSettlementStatus = mp.getSettlementStatus();
            mobilePhoneReceiptUrl = mp.getReceiptUrl();
        }

        String transferBankCode = null;
        String transferSettlementStatus = null;
        if (response.getTransfer() != null) {
            TossPaymentConfirmResponse.Transfer transfer = response.getTransfer();
            transferBankCode = transfer.getBankCode();
            transferSettlementStatus = transfer.getSettlementStatus();
        }

        String easyPayProvider = null;
        Integer easyPayAmount = null;
        Integer easyPayDiscountAmount = null;
        if (response.getEasyPay() != null) {
            TossPaymentConfirmResponse.EasyPay easyPay = response.getEasyPay();
            easyPayProvider = easyPay.getProvider();
            easyPayAmount = easyPay.getAmount();
            easyPayDiscountAmount = easyPay.getDiscountAmount();
        }

        String receiptUrl = response.getReceipt() != null ? response.getReceipt().getUrl() : null;
        String checkoutUrl = response.getCheckout() != null ? response.getCheckout().getUrl() : null;

        String failureCode = null;
        String failureMessage = null;
        if (response.getFailure() != null) {
            failureCode = response.getFailure().getCode();
            failureMessage = response.getFailure().getMessage();
        }
        if (response.getCode() != null) {
            failureCode = response.getCode();
            failureMessage = response.getMessage();
        }

        return new PgConfirmResult.TossPaymentDetail(
            response.getVersion(),
            response.getPaymentKey(),
            response.getType(),
            response.getOrderId(),
            response.getOrderName(),
            response.getMId(),
            response.getCurrency(),
            response.getMethod(),
            response.getTotalAmount(),
            response.getBalanceAmount(),
            response.getStatus(),
            TossPaymentUtils.parseDateTime(response.getRequestedAt()),
            TossPaymentUtils.parseDateTime(response.getApprovedAt()),
            response.isUseEscrow(),
            response.getLastTransactionKey(),
            response.getSuppliedAmount(),
            response.getVat(),
            response.isCultureExpense(),
            response.getTaxFreeAmount(),
            response.getTaxExemptionAmount(),
            response.isPartialCancelable(),
            cardAmount,
            cardIssuerCode,
            cardAcquirerCode,
            cardNumber,
            cardInstallmentPlanMonths,
            cardApproveNo,
            cardUseCardPoint,
            cardType,
            cardOwnerType,
            cardAcquireStatus,
            cardInterestFree,
            cardInterestPayer,
            virtualAccountType,
            virtualAccountNumber,
            virtualAccountBankCode,
            virtualAccountCustomerName,
            virtualAccountDueDate,
            virtualAccountRefundStatus,
            virtualAccountExpired,
            virtualAccountSettlementStatus,
            mobilePhoneCustomerMobilePhone,
            mobilePhoneSettlementStatus,
            mobilePhoneReceiptUrl,
            transferBankCode,
            transferSettlementStatus,
            easyPayProvider,
            easyPayAmount,
            easyPayDiscountAmount,
            receiptUrl,
            checkoutUrl,
            failureCode,
            failureMessage,
            response.getCountry()
        );
    }
}
