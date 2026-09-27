package com.tastyhouse.application.member.port.out.write;

import java.util.List;
import java.util.Optional;

public interface MemberDeliveryAddressStatePort {
    Optional<MemberDeliveryAddressState> findById(Long addressId);

    List<MemberDeliveryAddressState> findByMemberId(Long memberId);

    long countByMemberId(Long memberId);

    Optional<MemberDeliveryAddressState> findDefaultByMemberId(Long memberId);

    MemberDeliveryAddressState save(MemberDeliveryAddressState state);

    void deleteById(Long addressId);
}
