package com.tastyhouse.application.product.store;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.ProductImageChangeRequest;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;

import static org.assertj.core.api.Assertions.assertThat;

class ProductImageChangeRequestStateMapperTest {

    @Test
    @DisplayName("ProductImageChangeRequest → ProductImageChangeRequestState → ProductImageChangeRequest 왕복 시 모든 필드가 보존된다")
    void productImageChangeRequestRoundTrip() {
        ProductImageChangeRequest original = ProductImageChangeRequest.reconstitute(
            91L,
            ProductId.of(92L),
            UploadedFileId.of(93L),
            ApprovalStatus.REJECTED,
            "흐림",
            LocalDateTime.of(2026, 2, 10, 10, 10),
            LocalDateTime.of(2026, 2, 11, 10, 11)
        );

        ProductImageChangeRequest restored = ProductImageChangeRequestStateMapper.toDomain(ProductImageChangeRequestStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
