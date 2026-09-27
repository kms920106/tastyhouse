package com.tastyhouse.application.product.store;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.product.model.ProductVegetarianRequest;
import com.tastyhouse.domain.product.model.VegetarianType;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;

import static org.assertj.core.api.Assertions.assertThat;

class ProductVegetarianRequestStateMapperTest {

    @Test
    @DisplayName("ProductVegetarianRequest → ProductVegetarianRequestState → ProductVegetarianRequest 왕복 시 모든 필드가 보존된다")
    void productVegetarianRequestRoundTrip() {
        ProductVegetarianRequest original = ProductVegetarianRequest.reconstitute(
            111L,
            ProductId.of(112L),
            VegetarianType.LACTO_OVO,
            "두부",
            "설명",
            ApprovalStatus.PENDING,
            "반려사유",
            LocalDateTime.of(2026, 2, 14, 10, 14),
            LocalDateTime.of(2026, 2, 15, 10, 15)
        );

        ProductVegetarianRequest restored = ProductVegetarianRequestStateMapper.toDomain(ProductVegetarianRequestStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
