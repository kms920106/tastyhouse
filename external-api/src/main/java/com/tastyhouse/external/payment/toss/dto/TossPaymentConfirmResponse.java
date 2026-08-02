package com.tastyhouse.external.payment.toss.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

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

    public String getMId() {
        return this.mId;
    }

    public void setMId(String mId) {
        this.mId = mId;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPaymentKey() {
        return this.paymentKey;
    }

    public void setPaymentKey(String paymentKey) {
        this.paymentKey = paymentKey;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLastTransactionKey() {
        return this.lastTransactionKey;
    }

    public void setLastTransactionKey(String lastTransactionKey) {
        this.lastTransactionKey = lastTransactionKey;
    }

    public String getOrderId() {
        return this.orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderName() {
        return this.orderName;
    }

    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

    public String getRequestedAt() {
        return this.requestedAt;
    }

    public void setRequestedAt(String requestedAt) {
        this.requestedAt = requestedAt;
    }

    public String getApprovedAt() {
        return this.approvedAt;
    }

    public void setApprovedAt(String approvedAt) {
        this.approvedAt = approvedAt;
    }

    public boolean isUseEscrow() {
        return this.useEscrow;
    }

    public void setUseEscrow(boolean useEscrow) {
        this.useEscrow = useEscrow;
    }

    public boolean isCultureExpense() {
        return this.cultureExpense;
    }

    public void setCultureExpense(boolean cultureExpense) {
        this.cultureExpense = cultureExpense;
    }

    public Card getCard() {
        return this.card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public VirtualAccount getVirtualAccount() {
        return this.virtualAccount;
    }

    public void setVirtualAccount(VirtualAccount virtualAccount) {
        this.virtualAccount = virtualAccount;
    }

    public Transfer getTransfer() {
        return this.transfer;
    }

    public void setTransfer(Transfer transfer) {
        this.transfer = transfer;
    }

    public MobilePhone getMobilePhone() {
        return this.mobilePhone;
    }

    public void setMobilePhone(MobilePhone mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    public EasyPay getEasyPay() {
        return this.easyPay;
    }

    public void setEasyPay(EasyPay easyPay) {
        this.easyPay = easyPay;
    }

    public String getCountry() {
        return this.country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Failure getFailure() {
        return this.failure;
    }

    public void setFailure(Failure failure) {
        this.failure = failure;
    }

    public boolean isPartialCancelable() {
        return this.isPartialCancelable;
    }

    public void setPartialCancelable(boolean partialCancelable) {
        this.isPartialCancelable = partialCancelable;
    }

    public Receipt getReceipt() {
        return this.receipt;
    }

    public void setReceipt(Receipt receipt) {
        this.receipt = receipt;
    }

    public Checkout getCheckout() {
        return this.checkout;
    }

    public void setCheckout(Checkout checkout) {
        this.checkout = checkout;
    }

    public String getCurrency() {
        return this.currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Integer getTotalAmount() {
        return this.totalAmount;
    }

    public void setTotalAmount(Integer totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Integer getBalanceAmount() {
        return this.balanceAmount;
    }

    public void setBalanceAmount(Integer balanceAmount) {
        this.balanceAmount = balanceAmount;
    }

    public Integer getSuppliedAmount() {
        return this.suppliedAmount;
    }

    public void setSuppliedAmount(Integer suppliedAmount) {
        this.suppliedAmount = suppliedAmount;
    }

    public Integer getVat() {
        return this.vat;
    }

    public void setVat(Integer vat) {
        this.vat = vat;
    }

    public Integer getTaxFreeAmount() {
        return this.taxFreeAmount;
    }

    public void setTaxFreeAmount(Integer taxFreeAmount) {
        this.taxFreeAmount = taxFreeAmount;
    }

    public Integer getTaxExemptionAmount() {
        return this.taxExemptionAmount;
    }

    public void setTaxExemptionAmount(Integer taxExemptionAmount) {
        this.taxExemptionAmount = taxExemptionAmount;
    }

    public String getMethod() {
        return this.method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCancels(java.util.List<Cancel> cancels) {
        this.cancels = cancels;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

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

        public String getIssuerCode() {
            return this.issuerCode;
        }

        public void setIssuerCode(String issuerCode) {
            this.issuerCode = issuerCode;
        }

        public String getAcquirerCode() {
            return this.acquirerCode;
        }

        public void setAcquirerCode(String acquirerCode) {
            this.acquirerCode = acquirerCode;
        }

        public String getNumber() {
            return this.number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public Integer getInstallmentPlanMonths() {
            return this.installmentPlanMonths;
        }

        public void setInstallmentPlanMonths(Integer installmentPlanMonths) {
            this.installmentPlanMonths = installmentPlanMonths;
        }

        public boolean isInterestFree() {
            return this.isInterestFree;
        }

        public void setInterestFree(boolean interestFree) {
            this.isInterestFree = interestFree;
        }

        public String getInterestPayer() {
            return this.interestPayer;
        }

        public void setInterestPayer(String interestPayer) {
            this.interestPayer = interestPayer;
        }

        public String getApproveNo() {
            return this.approveNo;
        }

        public void setApproveNo(String approveNo) {
            this.approveNo = approveNo;
        }

        public boolean isUseCardPoint() {
            return this.useCardPoint;
        }

        public void setUseCardPoint(boolean useCardPoint) {
            this.useCardPoint = useCardPoint;
        }

        public String getCardType() {
            return this.cardType;
        }

        public void setCardType(String cardType) {
            this.cardType = cardType;
        }

        public String getOwnerType() {
            return this.ownerType;
        }

        public void setOwnerType(String ownerType) {
            this.ownerType = ownerType;
        }

        public String getAcquireStatus() {
            return this.acquireStatus;
        }

        public void setAcquireStatus(String acquireStatus) {
            this.acquireStatus = acquireStatus;
        }

        public Integer getAmount() {
            return this.amount;
        }

        public void setAmount(Integer amount) {
            this.amount = amount;
        }
    }

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

        public String getAccountType() {
            return this.accountType;
        }

        public void setAccountType(String accountType) {
            this.accountType = accountType;
        }

        public String getAccountNumber() {
            return this.accountNumber;
        }

        public void setAccountNumber(String accountNumber) {
            this.accountNumber = accountNumber;
        }

        public String getBankCode() {
            return this.bankCode;
        }

        public void setBankCode(String bankCode) {
            this.bankCode = bankCode;
        }

        public String getCustomerName() {
            return this.customerName;
        }

        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }

        public String getDueDate() {
            return this.dueDate;
        }

        public void setDueDate(String dueDate) {
            this.dueDate = dueDate;
        }

        public String getRefundStatus() {
            return this.refundStatus;
        }

        public void setRefundStatus(String refundStatus) {
            this.refundStatus = refundStatus;
        }

        public boolean isExpired() {
            return this.expired;
        }

        public void setExpired(boolean expired) {
            this.expired = expired;
        }

        public String getSettlementStatus() {
            return this.settlementStatus;
        }

        public void setSettlementStatus(String settlementStatus) {
            this.settlementStatus = settlementStatus;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Transfer {
        private String bankCode;
        private String settlementStatus;

        public String getBankCode() {
            return this.bankCode;
        }

        public void setBankCode(String bankCode) {
            this.bankCode = bankCode;
        }

        public String getSettlementStatus() {
            return this.settlementStatus;
        }

        public void setSettlementStatus(String settlementStatus) {
            this.settlementStatus = settlementStatus;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MobilePhone {
        private String customerMobilePhone;
        private String settlementStatus;
        private String receiptUrl;

        public String getCustomerMobilePhone() {
            return this.customerMobilePhone;
        }

        public void setCustomerMobilePhone(String customerMobilePhone) {
            this.customerMobilePhone = customerMobilePhone;
        }

        public String getSettlementStatus() {
            return this.settlementStatus;
        }

        public void setSettlementStatus(String settlementStatus) {
            this.settlementStatus = settlementStatus;
        }

        public String getReceiptUrl() {
            return this.receiptUrl;
        }

        public void setReceiptUrl(String receiptUrl) {
            this.receiptUrl = receiptUrl;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EasyPay {
        private String provider;
        private Integer amount;
        private Integer discountAmount;

        public String getProvider() {
            return this.provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public Integer getAmount() {
            return this.amount;
        }

        public void setAmount(Integer amount) {
            this.amount = amount;
        }

        public Integer getDiscountAmount() {
            return this.discountAmount;
        }

        public void setDiscountAmount(Integer discountAmount) {
            this.discountAmount = discountAmount;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Failure {
        private String code;
        private String message;

        public String getCode() {
            return this.code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return this.message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Receipt {
        private String url;

        public String getUrl() {
            return this.url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Checkout {
        private String url;

        public String getUrl() {
            return this.url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

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

        public void setCancelReason(String cancelReason) {
            this.cancelReason = cancelReason;
        }

        public void setCanceledAt(String canceledAt) {
            this.canceledAt = canceledAt;
        }

        public void setCancelAmount(Integer cancelAmount) {
            this.cancelAmount = cancelAmount;
        }

        public void setTaxFreeAmount(Integer taxFreeAmount) {
            this.taxFreeAmount = taxFreeAmount;
        }

        public void setTaxExemptionAmount(Integer taxExemptionAmount) {
            this.taxExemptionAmount = taxExemptionAmount;
        }

        public void setRefundableAmount(Integer refundableAmount) {
            this.refundableAmount = refundableAmount;
        }

        public void setTransferDiscountAmount(Integer transferDiscountAmount) {
            this.transferDiscountAmount = transferDiscountAmount;
        }

        public void setEasyPayDiscountAmount(Integer easyPayDiscountAmount) {
            this.easyPayDiscountAmount = easyPayDiscountAmount;
        }

        public void setTransactionKey(String transactionKey) {
            this.transactionKey = transactionKey;
        }

        public void setReceiptKey(String receiptKey) {
            this.receiptKey = receiptKey;
        }

        public void setCancelStatus(String cancelStatus) {
            this.cancelStatus = cancelStatus;
        }

        public void setCancelRequestId(String cancelRequestId) {
            this.cancelRequestId = cancelRequestId;
        }
    }

    public boolean isSuccess() {
        return "DONE".equals(status) && code == null;
    }

    public boolean isError() {
        return code != null;
    }
}
