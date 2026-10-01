package com.tastyhouse.infrastructure.member.persistence;

import com.tastyhouse.domain.member.model.MemberDeliveryAddress;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.region.vo.AdminDongId;

final class MemberDeliveryAddressMapper {

    private MemberDeliveryAddressMapper() {
    }

    static MemberDeliveryAddress toDomain(MemberDeliveryAddressJpaEntity entity) {
        return MemberDeliveryAddress.reconstitute(
            entity.getId(),
            entity.getMemberId() == null ? null : MemberId.of(entity.getMemberId()),
            entity.getAlias(),
            entity.getRoadAddress(),
            entity.getLotAddress(),
            entity.getDetailAddress(),
            entity.getAdminDongId() == null ? null : AdminDongId.of(entity.getAdminDongId()),
            entity.getLatitude(),
            entity.getLongitude(),
            entity.isDefaultAddress(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static MemberDeliveryAddressJpaEntity toEntity(MemberDeliveryAddress address) {
        return MemberDeliveryAddressJpaEntity.create(
            address.getMemberId() == null ? null : address.getMemberId().value(),
            address.getAlias(),
            address.getRoadAddress(),
            address.getLotAddress(),
            address.getDetailAddress(),
            address.getAdminDongId() == null ? null : address.getAdminDongId().value(),
            address.getLatitude(),
            address.getLongitude(),
            address.isDefaultAddress()
        );
    }

    static void applyChanges(MemberDeliveryAddressJpaEntity entity, MemberDeliveryAddress address) {
        entity.applyChanges(
            address.getAlias(),
            address.getRoadAddress(),
            address.getLotAddress(),
            address.getDetailAddress(),
            address.getAdminDongId() == null ? null : address.getAdminDongId().value(),
            address.getLatitude(),
            address.getLongitude(),
            address.isDefaultAddress()
        );
    }
}
