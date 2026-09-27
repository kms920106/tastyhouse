package com.tastyhouse.infrastructure.shop.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.shop.model.ShopCeoAssignmentActionType;
import com.tastyhouse.domain.shop.model.ShopCeoAssignmentHistory;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ShopCeoAssignmentHistoryMapperTest {

    @Test
    @DisplayName("ShopCeoAssignmentHistory 도메인 → 엔티티 변환 시 컬럼 값이 보존된다")
    void domainToEntity() {
        ShopCeoAssignmentHistory original = ShopCeoAssignmentHistory.reconstitute(
            107L,
            ShopId.of(108L),
            CeoId.of(109L),
            ShopCeoAssignmentActionType.GRANT,
            111L,
            LocalDateTime.of(2026, 1, 13, 10, 12)
        );

        ShopCeoAssignmentHistoryJpaEntity entity = ShopCeoAssignmentHistoryMapper.toEntity(original);

        assertThat(entity.getShopId()).isEqualTo(108L);
        assertThat(entity.getCeoId()).isEqualTo(109L);
        assertThat(entity.getActionType()).isEqualTo("GRANT");
        assertThat(entity.getActorAdminId()).isEqualTo(111L);
    }

    @Test
    @DisplayName("ShopCeoAssignmentHistory 엔티티 → 도메인 변환 시 모든 필드가 보존된다")
    void entityToDomain() {
        ShopCeoAssignmentHistoryJpaEntity entity = ShopCeoAssignmentHistoryJpaEntity.create(108L, 109L, "GRANT", 111L);
        ReflectionTestUtils.setField(entity, "id", 107L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 1, 13, 10, 12));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 1, 14, 10, 12));

        ShopCeoAssignmentHistory domain = ShopCeoAssignmentHistoryMapper.toDomain(entity);

        assertThat(domain).usingRecursiveComparison().isEqualTo(ShopCeoAssignmentHistory.reconstitute(
            107L,
            ShopId.of(108L),
            CeoId.of(109L),
            ShopCeoAssignmentActionType.GRANT,
            111L,
            LocalDateTime.of(2026, 1, 13, 10, 12)
        ));
    }
}
