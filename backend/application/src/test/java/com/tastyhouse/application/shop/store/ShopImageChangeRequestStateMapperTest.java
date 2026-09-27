package com.tastyhouse.application.shop.store;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.model.ShopImageChangeRequest;
import com.tastyhouse.domain.shop.model.ShopImageType;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ShopImageChangeRequestStateMapperTest {

    @Test
    @DisplayName("ShopImageChangeRequest → ShopImageChangeRequestState → ShopImageChangeRequest 왕복 시 모든 필드가 보존된다")
    void shopImageChangeRequestRoundTrip() {
        ShopImageChangeRequest original = ShopImageChangeRequest.reconstitute(
            131L,
            ShopId.of(132L),
            ShopImageType.THUMBNAIL,
            UploadedFileId.of(134L),
            ApprovalStatus.CANCELED,
            "v36",
            LocalDateTime.of(2026, 1, 10, 10, 37),
            LocalDateTime.of(2026, 1, 11, 10, 38)
        );

        ShopImageChangeRequest restored = ShopImageChangeRequestStateMapper.toDomain(ShopImageChangeRequestStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
