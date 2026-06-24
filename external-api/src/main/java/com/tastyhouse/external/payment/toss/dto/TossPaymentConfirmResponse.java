package com.tastyhouse.external.payment.toss.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TossPaymentConfirmResponse {

    private String mId;
    private String version;
    private String paymentKey;
    private String status;
    private String lastTransactionKey;
    private String orderId;
    private String orderName;
    private String requestedAt;
    private String approvedAt;
    private boolean useEscrow;
    private boolean cultureExpense;
    private Card card;
    private VirtualAccount virtualAccount;
    private Transfer transfer;
    private MobilePhone mobilePhone;
    private EasyPay easyPay;
    private String country;
    private Failure failure;
    @JsonProperty("isPartialCancelable")
    private boolean isPartialCancelable;
    private Receipt receipt;
    private Checkout checkout;
    private String currency;
    private Integer totalAmount;
    private Integer balanceAmount;
    private Integer suppliedAmount;
    private Integer vat;
    private Integer taxFreeAmount;
    private Integer taxExemptionAmount;
    private String method;
    private String type;
    private java.util.List<Cancel> cancels;

    // 에러 응답
    private String code;
    private String message;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Card {
        private String issuerCode;
        private String acquirerCode;
        private String number;
        private Integer installmentPlanMonths;
        @JsonProperty("isInterestFree")
        private boolean isInterestFree;
        private String interestPayer;
        private String approveNo;
        private boolean useCardPoint;
        private String cardType;
        private String ownerType;
        private String acquireStatus;
        private Integer amount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VirtualAccount {
        private String accountType;
        private String accountNumber;
        private String bankCode;
        private String customerName;
        private String dueDate;
        private String refundStatus;
        private boolean expired;
        private String settlementStatus;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Transfer {
        private String bankCode;
        private String settlementStatus;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MobilePhone {
        private String customerMobilePhone;
        private String settlementStatus;
        private String receiptUrl;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EasyPay {
        private String provider;
        private Integer amount;
        private Integer discountAmount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Failure {
        private String code;
        private String message;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Receipt {
        private String url;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Checkout {
        private String url;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Cancel {
        private String cancelReason;
        private String canceledAt;
        private Integer cancelAmount;
        private Integer taxFreeAmount;
        private Integer taxExemptionAmount;
        private Integer refundableAmount;
        private Integer transferDiscountAmount;
        private Integer easyPayDiscountAmount;
        private String transactionKey;
        private String receiptKey;
        private String cancelStatus;
        private String cancelRequestId;
    }

    public boolean isSuccess() {
        return "DONE".equals(status) && code == null;
    }

    public boolean isError() {
        return code != null;
    }
}
