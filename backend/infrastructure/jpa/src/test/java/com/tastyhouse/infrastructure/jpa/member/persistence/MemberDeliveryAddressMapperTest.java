package com.tastyhouse.infrastructure.jpa.member.persistence;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.member.model.MemberDeliveryAddress;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.region.vo.AdminDongId;

import static org.assertj.core.api.Assertions.assertThat;

class MemberDeliveryAddressMapperTest {

    @Test
    @DisplayName("MemberDeliveryAddress → 엔티티 변환 시 모든 컬럼 값이 보존된다")
    void toEntity() {
        MemberDeliveryAddressJpaEntity entity = MemberDeliveryAddressMapper.toEntity(address());

        assertThat(entity.getMemberId()).isEqualTo(92L);
        assertThat(entity.getAlias()).isEqualTo("집");
        assertThat(entity.getRoadAddress()).isEqualTo("도로명 주소");
        assertThat(entity.getLotAddress()).isEqualTo("지번 주소");
        assertThat(entity.getDetailAddress()).isEqualTo("상세 주소");
        assertThat(entity.getAdminDongId()).isEqualTo(93L);
        assertThat(entity.getLatitude()).isEqualTo(new BigDecimal("37.123456"));
        assertThat(entity.getLongitude()).isEqualTo(new BigDecimal("127.654321"));
        assertThat(entity.isDefaultAddress()).isTrue();
    }

    @Test
    @DisplayName("엔티티 → MemberDeliveryAddress 변환 시 모든 필드가 보존된다")
    void toDomain() {
        MemberDeliveryAddress original = address();
        MemberDeliveryAddressJpaEntity entity = MemberDeliveryAddressMapper.toEntity(original);
        ReflectionTestUtils.setField(entity, "id", original.getId());
        ReflectionTestUtils.setField(entity, "createdAt", original.getCreatedAt());
        ReflectionTestUtils.setField(entity, "updatedAt", original.getUpdatedAt());

        MemberDeliveryAddress restored = MemberDeliveryAddressMapper.toDomain(entity);

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    private static MemberDeliveryAddress address() {
        return MemberDeliveryAddress.reconstitute(
            91L, MemberId.of(92L), "집", "도로명 주소", "지번 주소", "상세 주소", AdminDongId.of(93L),
            new BigDecimal("37.123456"), new BigDecimal("127.654321"), true,
            LocalDateTime.of(2026, 5, 5, 5, 5),
            LocalDateTime.of(2026, 6, 6, 6, 6));
    }
}
