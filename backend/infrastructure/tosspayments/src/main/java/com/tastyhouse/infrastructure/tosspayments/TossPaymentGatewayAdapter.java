package com.tastyhouse.infrastructure.tosspayments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.tastyhouse.application.payment.port.out.PgCancelResult;
import com.tastyhouse.application.payment.port.out.PgConfirmFailure;
import com.tastyhouse.application.payment.port.out.PgConfirmResult;
import com.tastyhouse.application.payment.port.out.PgPaymentLookupResult;
import com.tastyhouse.application.payment.port.out.PgPaymentLookupStatus;
import com.tastyhouse.application.payment.port.out.PgProviderCode;
import com.tastyhouse.application.payment.port.out.PgProviderGatewayPort;
import com.tastyhouse.application.payment.port.out.TossPaymentDetail;

@Component
class TossPaymentGatewayAdapter implements PgProviderGatewayPort {

    private static final Logger log = LoggerFactory.getLogger(TossPaymentGatewayAdapter.class);

    private static final String ALREADY_PROCESSED_PAYMENT = "ALREADY_PROCESSED_PAYMENT";
    private static final String NOT_FOUND_PAYMENT = "NOT_FOUND_PAYMENT";
    private static final String ORDER_ID_MISMATCH = "ORDER_ID_MISMATCH";

    private final TossPaymentClient tossPaymentClient;

    public TossPaymentGatewayAdapter(TossPaymentClient tossPaymentClient) {
        this.tossPaymentClient = tossPaymentClient;
    }

    @Override
    public PgProviderCode provider() {
        return PgProviderCode.TOSS;
    }

    @Override
    public PgConfirmResult confirmPayment(Long paymentId, String paymentKey, String pgOrderId, int amount) {
        TossPaymentConfirmResponse response = tossPaymentClient.confirmPayment(paymentKey, pgOrderId, amount);

        TossPaymentDetail detail = buildTossPaymentDetail(response);

        if (response.isError()) {
            PgConfirmFailure failure = toFailure(response.getCode());
            if (failure == PgConfirmFailure.ALREADY_PROCESSED) {
                log.warn("Toss confirm already processed. pgOrderId: {}, errorCode: {}, msg: {}",
                    pgOrderId, response.getCode(), response.getMessage());
            } else {
                log.error("Toss confirm failed. pgOrderId: {}, errorCode: {}, msg: {}",
                    pgOrderId, response.getCode(), response.getMessage());
            }
            return failureResult(response.getCode(), response.getMessage(), failure, detail);
        }

        return approvedResult(response, detail);
    }

    @Override
    public PgPaymentLookupResult lookupPayment(String pgOrderId) {
        TossPaymentConfirmResponse response = tossPaymentClient.lookupPaymentByOrderId(pgOrderId);

        TossPaymentDetail detail = buildTossPaymentDetail(response);

        if (response.isError()) {
            if (NOT_FOUND_PAYMENT.equals(response.getCode())) {
                return notApproved(failureResult(response.getCode(), response.getMessage(), PgConfirmFailure.REJECTED, detail));
            }
            log.warn("Toss lookup unconfirmed. pgOrderId: {}, errorCode: {}, msg: {}",
                pgOrderId, response.getCode(), response.getMessage());
            return unknown(failureResult(response.getCode(), response.getMessage(), PgConfirmFailure.UNKNOWN, detail));
        }

        if (!pgOrderId.equals(response.getOrderId())) {
            log.error("Toss lookup returned another order. requested pgOrderId: {}, response orderId: {}",
                pgOrderId, response.getOrderId());
            return unknown(failureResult(ORDER_ID_MISMATCH, "토스 조회 응답의 주문번호가 요청과 다릅니다.", PgConfirmFailure.UNKNOWN, null));
        }

        String status = response.getStatus();
        if ("DONE".equals(status)) {
            return new PgPaymentLookupResult(PgPaymentLookupStatus.APPROVED, approvedResult(response, detail));
        }
        if ("ABORTED".equals(status) || "EXPIRED".equals(status) || "CANCELED".equals(status)) {
            return notApproved(failureResult(status, "토스 결제 상태: " + status, PgConfirmFailure.REJECTED, detail));
        }

        log.warn("Toss lookup returned unexpected status. pgOrderId: {}, status: {}", pgOrderId, status);
        return unknown(failureResult(status, "토스 결제 상태: " + status, PgConfirmFailure.UNKNOWN, detail));
    }

    private static PgPaymentLookupResult notApproved(PgConfirmResult result) {
        return new PgPaymentLookupResult(PgPaymentLookupStatus.NOT_APPROVED, result);
    }

    private static PgPaymentLookupResult unknown(PgConfirmResult result) {
        return new PgPaymentLookupResult(PgPaymentLookupStatus.UNKNOWN, result);
    }

    private static PgConfirmResult failureResult(
        String errorCode,
        String errorMessage,
        PgConfirmFailure failure,
        TossPaymentDetail detail
    ) {
        return new PgConfirmResult(
            false, null, null, null, null, null, null, null, null,
            errorCode, errorMessage, failure, detail
        );
    }

    private static PgConfirmResult approvedResult(TossPaymentConfirmResponse response, TossPaymentDetail detail) {
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
            null,
            detail
        );
    }

    private static PgConfirmFailure toFailure(String errorCode) {
        if (ALREADY_PROCESSED_PAYMENT.equals(errorCode)) {
            return PgConfirmFailure.ALREADY_PROCESSED;
        }
        if (TossPaymentClient.PG_SERVER_ERROR.equals(errorCode)
            || TossPaymentClient.SYSTEM_ERROR.equals(errorCode)
            || TossPaymentClient.UNKNOWN_ERROR.equals(errorCode)
            || TossPaymentClient.PG_UNREADABLE_RESPONSE.equals(errorCode)) {
            return PgConfirmFailure.UNKNOWN;
        }
        return PgConfirmFailure.REJECTED;
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

    private TossPaymentDetail buildTossPaymentDetail(TossPaymentConfirmResponse response) {
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

        return new TossPaymentDetail(
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
