package com.tastyhouse.infrastructure.member.persistence;

import com.tastyhouse.application.member.port.out.write.MemberDeliveryAddressState;

final class MemberDeliveryAddressMapper {
    private MemberDeliveryAddressMapper() {
    }

    static MemberDeliveryAddressState toState(MemberDeliveryAddressJpaEntity entity) {
        return new MemberDeliveryAddressState(
            entity.getId(),
            entity.getMemberId(),
            entity.getAlias(),
            entity.getRoadAddress(),
            entity.getLotAddress(),
            entity.getDetailAddress(),
            entity.getAdminDongId(),
            entity.getLatitude(),
            entity.getLongitude(),
            entity.isDefaultAddress(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static MemberDeliveryAddressJpaEntity toEntity(MemberDeliveryAddressState state) {
        return MemberDeliveryAddressJpaEntity.create(
            state.memberId(),
            state.alias(),
            state.roadAddress(),
            state.lotAddress(),
            state.detailAddress(),
            state.adminDongId(),
            state.latitude(),
            state.longitude(),
            state.defaultAddress()
        );
    }

    static void applyChanges(MemberDeliveryAddressJpaEntity entity, MemberDeliveryAddressState state) {
        entity.applyChanges(
            state.alias(),
            state.roadAddress(),
            state.lotAddress(),
            state.detailAddress(),
            state.adminDongId(),
            state.latitude(),
            state.longitude(),
            state.defaultAddress()
        );
    }
}
