package com.tastyhouse.application.member.store;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.application.member.port.out.write.MemberDeliveryAddressStatePort;
import com.tastyhouse.domain.member.model.MemberDeliveryAddress;
import com.tastyhouse.domain.member.vo.MemberId;

public class MemberDeliveryAddressStore implements MemberDeliveryAddressRepository {
    private final MemberDeliveryAddressStatePort memberDeliveryAddressStatePort;

    public MemberDeliveryAddressStore(MemberDeliveryAddressStatePort memberDeliveryAddressStatePort) {
        this.memberDeliveryAddressStatePort = memberDeliveryAddressStatePort;
    }

    @Override
    public Optional<MemberDeliveryAddress> findById(Long addressId) {
        return memberDeliveryAddressStatePort.findById(addressId).map(MemberDeliveryAddressStateMapper::toDomain);
    }

    @Override
    public List<MemberDeliveryAddress> findByMemberId(MemberId memberId) {
        return memberDeliveryAddressStatePort.findByMemberId(memberId.value())
            .stream()
            .map(MemberDeliveryAddressStateMapper::toDomain)
            .toList();
    }

    @Override
    public long countByMemberId(MemberId memberId) {
        return memberDeliveryAddressStatePort.countByMemberId(memberId.value());
    }

    @Override
    public Optional<MemberDeliveryAddress> findDefaultByMemberId(MemberId memberId) {
        return memberDeliveryAddressStatePort.findDefaultByMemberId(memberId.value())
            .map(MemberDeliveryAddressStateMapper::toDomain);
    }

    @Override
    public MemberDeliveryAddress save(MemberDeliveryAddress memberDeliveryAddress) {
        return MemberDeliveryAddressStateMapper.toDomain(
            memberDeliveryAddressStatePort.save(MemberDeliveryAddressStateMapper.toState(memberDeliveryAddress))
        );
    }

    @Override
    public void deleteById(Long addressId) {
        memberDeliveryAddressStatePort.deleteById(addressId);
    }
}
