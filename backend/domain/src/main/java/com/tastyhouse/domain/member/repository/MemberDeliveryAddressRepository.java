package com.tastyhouse.domain.member.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.member.model.MemberDeliveryAddress;
import com.tastyhouse.domain.member.vo.MemberId;

public interface MemberDeliveryAddressRepository {
    Optional<MemberDeliveryAddress> findById(Long addressId);

    List<MemberDeliveryAddress> findByMemberId(MemberId memberId);

    long countByMemberId(MemberId memberId);

    Optional<MemberDeliveryAddress> findDefaultByMemberId(MemberId memberId);

    MemberDeliveryAddress save(MemberDeliveryAddress memberDeliveryAddress);

    void deleteById(Long addressId);
}
