package com.tastyhouse.application.payment.store;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.payment.model.TossPaymentRecord;
import com.tastyhouse.domain.payment.vo.PaymentId;

import static org.assertj.core.api.Assertions.assertThat;

class TossPaymentRecordStateMapperTest {

    @Test
    @DisplayName("TossPaymentRecord → TossPaymentRecordState → TossPaymentRecord 왕복 시 57개 필드가 모두 보존된다")
    void tossPaymentRecordRoundTrip() {
        TossPaymentRecord original = TossPaymentRecord.reconstitute(
            1001L,
            PaymentId.of(1002L),
            "version-3",
            "paymentKey-4",
            "type-5",
            "orderId-6",
            "orderName-7",
            "mId-8",
            "currency-9",
            "method-10",
            1011,
            1012,
            "status-13",
            LocalDateTime.of(2026, 5, 3, 10, 1),
            LocalDateTime.of(2026, 5, 3, 10, 2),
            true,
            "lastTransactionKey-17",
            1018,
            1019,
            false,
            1021,
            1022,
            true,
            1024,
            "cardIssuerCode-25",
            "cardAcquirerCode-26",
            "cardNumber-27",
            1028,
            "cardApproveNo-29",
            false,
            "cardType-31",
            "cardOwnerType-32",
            "cardAcquireStatus-33",
            true,
            "cardInterestPayer-35",
            "virtualAccountType-36",
            "virtualAccountNumber-37",
            "virtualAccountBankCode-38",
            "virtualAccountCustomerName-39",
            LocalDateTime.of(2026, 5, 3, 10, 3),
            "virtualAccountRefundStatus-41",
            false,
            "virtualAccountSettlementStatus-43",
            "mobilePhoneCustomerMobilePhone-44",
            "mobilePhoneSettlementStatus-45",
            "mobilePhoneReceiptUrl-46",
            "transferBankCode-47",
            "transferSettlementStatus-48",
            "easyPayProvider-49",
            1050,
            1051,
            "receiptUrl-52",
            "checkoutUrl-53",
            "failureCode-54",
            "failureMessage-55",
            "country-56",
            LocalDateTime.of(2026, 5, 3, 10, 4)
        );

        TossPaymentRecord restored =
            TossPaymentRecordStateMapper.toDomain(TossPaymentRecordStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
