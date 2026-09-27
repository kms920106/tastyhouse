package com.tastyhouse.application.product.store;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.StorePriceVerification;
import com.tastyhouse.domain.product.model.StorePriceVerificationItem;
import com.tastyhouse.domain.product.model.StorePriceVerificationStatus;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductPriceId;
import com.tastyhouse.domain.product.vo.StorePriceVerificationId;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class StorePriceVerificationStateMapperTest {

    @Test
    @DisplayName("StorePriceVerification → StorePriceVerificationState → StorePriceVerification 왕복 시 모든 필드가 보존된다")
    void storePriceVerificationRoundTrip() {
        StorePriceVerification original = StorePriceVerification.reconstitute(
            231L,
            ShopId.of(232L),
            UploadedFileId.of(233L),
            StorePriceVerificationStatus.IN_PROGRESS,
            "거절",
            234L,
            LocalDateTime.of(2026, 2, 24, 10, 24),
            LocalDateTime.of(2026, 2, 25, 10, 25),
            LocalDateTime.of(2026, 2, 26, 10, 26)
        );

        StorePriceVerification restored = StorePriceVerificationStateMapper.toDomain(StorePriceVerificationStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("StorePriceVerificationItem → StorePriceVerificationItemState → StorePriceVerificationItem 왕복 시 모든 필드가 보존된다")
    void storePriceVerificationItemRoundTrip() {
        StorePriceVerificationItem original = StorePriceVerificationItem.reconstitute(
            241L,
            StorePriceVerificationId.of(242L),
            ProductId.of(243L),
            ProductPriceId.of(244L),
            9900,
            true,
            LocalDateTime.of(2026, 2, 27, 10, 27),
            LocalDateTime.of(2026, 2, 28, 10, 28)
        );

        StorePriceVerificationItem restored = StorePriceVerificationStateMapper.toDomain(StorePriceVerificationStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
