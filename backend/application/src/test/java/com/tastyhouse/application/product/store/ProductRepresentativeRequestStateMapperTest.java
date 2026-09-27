package com.tastyhouse.application.product.store;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.product.model.ProductRepresentativeRequest;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ProductRepresentativeRequestStateMapperTest {

    @Test
    @DisplayName("ProductRepresentativeRequest → ProductRepresentativeRequestState → ProductRepresentativeRequest 왕복 시 모든 필드가 보존된다")
    void productRepresentativeRequestRoundTrip() {
        ProductRepresentativeRequest original = ProductRepresentativeRequest.reconstitute(
            101L,
            ProductId.of(102L),
            ShopId.of(103L),
            ApprovalStatus.APPROVED,
            "사유",
            LocalDateTime.of(2026, 2, 12, 10, 12),
            LocalDateTime.of(2026, 2, 13, 10, 13)
        );

        ProductRepresentativeRequest restored = ProductRepresentativeRequestStateMapper.toDomain(ProductRepresentativeRequestStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
