package com.tastyhouse.infrastructure.member.persistence;

import com.tastyhouse.domain.member.model.MemberDeliveryAddress;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class MemberDeliveryAddressMapper {
    private MemberDeliveryAddressMapper() {
    }

    static MemberDeliveryAddress toDomain(MemberDeliveryAddressJpaEntity entity) {
        return MemberDeliveryAddress.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getMemberId(), MemberId::of),
            entity.getAlias(),
            entity.getRoadAddress(),
            entity.getLotAddress(),
            entity.getDetailAddress(),
            IdMapping.vo(entity.getAdminDongId(), AdminDongId::of),
            entity.getLatitude(),
            entity.getLongitude(),
            entity.isDefaultAddress(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static MemberDeliveryAddressJpaEntity toEntity(MemberDeliveryAddress domain) {
        return MemberDeliveryAddressJpaEntity.create(
            IdMapping.raw(domain.getMemberId(), MemberId::value),
            domain.getAlias(),
            domain.getRoadAddress(),
            domain.getLotAddress(),
            domain.getDetailAddress(),
            IdMapping.raw(domain.getAdminDongId(), AdminDongId::value),
            domain.getLatitude(),
            domain.getLongitude(),
            domain.isDefaultAddress()
        );
    }

    static void applyChanges(MemberDeliveryAddressJpaEntity entity, MemberDeliveryAddress domain) {
        entity.applyChanges(
            domain.getAlias(),
            domain.getRoadAddress(),
            domain.getLotAddress(),
            domain.getDetailAddress(),
            IdMapping.raw(domain.getAdminDongId(), AdminDongId::value),
            domain.getLatitude(),
            domain.getLongitude(),
            domain.isDefaultAddress()
        );
    }
}
