package com.tastyhouse.infrastructure.persistence.payment.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.payment.model.TossPaymentRecord;
import com.tastyhouse.domain.payment.vo.PaymentId;

import static org.assertj.core.api.Assertions.assertThat;

class TossPaymentRecordMapperTest {

    @Test
    @DisplayName("TossPaymentRecord → 엔티티 변환 시 55개 컬럼 값이 모두 보존된다")
    void tossPaymentRecordToEntity() {
        TossPaymentRecordJpaEntity expected = TossPaymentRecordJpaEntity.create(
            1002L,
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
            "country-56"
        );

        TossPaymentRecordJpaEntity entity = TossPaymentRecordMapper.toEntity(tossPaymentRecord());

        assertThat(entity.getPaymentId()).isEqualTo(1002L);
        assertThat(entity.getPaymentKey()).isEqualTo("paymentKey-4");
        assertThat(entity.getTotalAmount()).isEqualTo(1011);
        assertThat(entity.isUseEscrow()).isTrue();
        assertThat(entity.getVirtualAccountDueDate()).isEqualTo(LocalDateTime.of(2026, 5, 3, 10, 3));
        assertThat(entity.getCountry()).isEqualTo("country-56");
        assertThat(entity).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    @DisplayName("엔티티 → TossPaymentRecord 변환 시 id·생성 시각을 포함한 57개 필드가 모두 보존된다")
    void tossPaymentRecordToDomain() {
        TossPaymentRecord original = tossPaymentRecord();
        TossPaymentRecordJpaEntity entity = TossPaymentRecordMapper.toEntity(original);
        ReflectionTestUtils.setField(entity, "id", 1001L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 5, 3, 10, 4));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 5, 3, 10, 5));

        TossPaymentRecord restored = TossPaymentRecordMapper.toDomain(entity);

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    private static TossPaymentRecord tossPaymentRecord() {
        return TossPaymentRecord.reconstitute(
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
    }
}
