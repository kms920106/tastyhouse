package com.tastyhouse.application.member.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.member.model.MemberDeliveryAddress;
import com.tastyhouse.domain.member.vo.MemberId;

public interface MemberDeliveryAddressPersistencePort {

    Optional<MemberDeliveryAddress> findById(Long addressId);

    long countByMemberId(MemberId memberId);

    Optional<MemberDeliveryAddress> findDefaultByMemberId(MemberId memberId);

    MemberDeliveryAddress save(MemberDeliveryAddress memberDeliveryAddress);

    void deleteById(Long addressId);
}
