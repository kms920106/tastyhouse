package com.tastyhouse.external.payment.toss;

import com.tastyhouse.core.domain.payment.application.port.PgPaymentGateway;
import com.tastyhouse.core.domain.payment.application.port.dto.PgCancelResult;
import com.tastyhouse.core.domain.payment.application.port.dto.PgConfirmResult;
import com.tastyhouse.core.domain.payment.domain.model.TossPaymentRecord;
import com.tastyhouse.core.domain.payment.domain.repository.TossPaymentRecordRepository;
import com.tastyhouse.external.payment.toss.dto.TossPaymentConfirmResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TossPaymentGatewayAdapter implements PgPaymentGateway {

    private final TossPaymentClient tossPaymentClient;
    private final TossPaymentRecordRepository tossPaymentRecordRepository;

    @Override
    public PgConfirmResult confirmPayment(Long paymentId, String paymentKey, String pgOrderId, int amount) {
        TossPaymentConfirmResponse response = tossPaymentClient.confirmPayment(paymentKey, pgOrderId, amount);

        TossPaymentRecord record = buildTossPaymentRecord(paymentId, response);
        tossPaymentRecordRepository.save(record);

        if (response.isError()) {
            log.error("Toss confirm failed. pgOrderId: {}, errorCode: {}, msg: {}",
                pgOrderId, response.getCode(), response.getMessage());
            return new PgConfirmResult(
                false, null, null, null, null, null, null, null, null,
                response.getCode(), response.getMessage()
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
            null
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

    private TossPaymentRecord buildTossPaymentRecord(Long paymentId, TossPaymentConfirmResponse response) {
        TossPaymentRecord.TossPaymentRecordBuilder builder = TossPaymentRecord.builder()
            .paymentId(paymentId)
            .version(response.getVersion())
            .paymentKey(response.getPaymentKey())
            .type(response.getType())
            .orderId(response.getOrderId())
            .orderName(response.getOrderName())
            .mId(response.getMId())
            .currency(response.getCurrency())
            .method(response.getMethod())
            .totalAmount(response.getTotalAmount())
            .balanceAmount(response.getBalanceAmount())
            .status(response.getStatus())
            .requestedAt(TossPaymentUtils.parseDateTime(response.getRequestedAt()))
            .approvedAt(TossPaymentUtils.parseDateTime(response.getApprovedAt()))
            .useEscrow(response.getUseEscrow())
            .lastTransactionKey(response.getLastTransactionKey())
            .suppliedAmount(response.getSuppliedAmount())
            .vat(response.getVat())
            .cultureExpense(response.getCultureExpense())
            .taxFreeAmount(response.getTaxFreeAmount())
            .taxExemptionAmount(response.getTaxExemptionAmount())
            .isPartialCancelable(response.getIsPartialCancelable())
            .country(response.getCountry());

        if (response.getCard() != null) {
            TossPaymentConfirmResponse.Card card = response.getCard();
            builder.cardAmount(card.getAmount())
                .cardIssuerCode(card.getIssuerCode())
                .cardAcquirerCode(card.getAcquirerCode())
                .cardNumber(card.getNumber())
                .cardInstallmentPlanMonths(card.getInstallmentPlanMonths())
                .cardApproveNo(card.getApproveNo())
                .cardUseCardPoint(card.getUseCardPoint())
                .cardType(card.getCardType())
                .cardOwnerType(card.getOwnerType())
                .cardAcquireStatus(card.getAcquireStatus())
                .cardIsInterestFree(card.getIsInterestFree())
                .cardInterestPayer(card.getInterestPayer());
        }

        if (response.getVirtualAccount() != null) {
            TossPaymentConfirmResponse.VirtualAccount va = response.getVirtualAccount();
            builder.virtualAccountType(va.getAccountType())
                .virtualAccountNumber(va.getAccountNumber())
                .virtualAccountBankCode(va.getBankCode())
                .virtualAccountCustomerName(va.getCustomerName())
                .virtualAccountDueDate(TossPaymentUtils.parseDateTime(va.getDueDate()))
                .virtualAccountRefundStatus(va.getRefundStatus())
                .virtualAccountExpired(va.getExpired())
                .virtualAccountSettlementStatus(va.getSettlementStatus());
        }

        if (response.getMobilePhone() != null) {
            TossPaymentConfirmResponse.MobilePhone mp = response.getMobilePhone();
            builder.mobilePhoneCustomerMobilePhone(mp.getCustomerMobilePhone())
                .mobilePhoneSettlementStatus(mp.getSettlementStatus())
                .mobilePhoneReceiptUrl(mp.getReceiptUrl());
        }

        if (response.getTransfer() != null) {
            TossPaymentConfirmResponse.Transfer transfer = response.getTransfer();
            builder.transferBankCode(transfer.getBankCode())
                .transferSettlementStatus(transfer.getSettlementStatus());
        }

        if (response.getEasyPay() != null) {
            TossPaymentConfirmResponse.EasyPay easyPay = response.getEasyPay();
            builder.easyPayProvider(easyPay.getProvider())
                .easyPayAmount(easyPay.getAmount())
                .easyPayDiscountAmount(easyPay.getDiscountAmount());
        }

        if (response.getReceipt() != null) {
            builder.receiptUrl(response.getReceipt().getUrl());
        }

        if (response.getCheckout() != null) {
            builder.checkoutUrl(response.getCheckout().getUrl());
        }

        if (response.getFailure() != null) {
            builder.failureCode(response.getFailure().getCode())
                .failureMessage(response.getFailure().getMessage());
        }

        if (response.getCode() != null) {
            builder.failureCode(response.getCode())
                .failureMessage(response.getMessage());
        }

        return builder.build();
    }
}
