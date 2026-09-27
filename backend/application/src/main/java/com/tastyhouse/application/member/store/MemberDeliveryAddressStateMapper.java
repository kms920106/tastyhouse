package com.tastyhouse.application.member.store;

import com.tastyhouse.application.member.port.out.write.MemberDeliveryAddressState;
import com.tastyhouse.domain.member.model.MemberDeliveryAddress;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.region.vo.AdminDongId;

final class MemberDeliveryAddressStateMapper {
    private MemberDeliveryAddressStateMapper() {
    }

    static MemberDeliveryAddress toDomain(MemberDeliveryAddressState state) {
        return MemberDeliveryAddress.reconstitute(
            state.id(),
            state.memberId() == null ? null : MemberId.of(state.memberId()),
            state.alias(),
            state.roadAddress(),
            state.lotAddress(),
            state.detailAddress(),
            state.adminDongId() == null ? null : AdminDongId.of(state.adminDongId()),
            state.latitude(),
            state.longitude(),
            state.defaultAddress(),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static MemberDeliveryAddressState toState(MemberDeliveryAddress address) {
        return new MemberDeliveryAddressState(
            address.getId(),
            address.getMemberId() == null ? null : address.getMemberId().value(),
            address.getAlias(),
            address.getRoadAddress(),
            address.getLotAddress(),
            address.getDetailAddress(),
            address.getAdminDongId() == null ? null : address.getAdminDongId().value(),
            address.getLatitude(),
            address.getLongitude(),
            address.isDefaultAddress(),
            address.getCreatedAt(),
            address.getUpdatedAt()
        );
    }
}
