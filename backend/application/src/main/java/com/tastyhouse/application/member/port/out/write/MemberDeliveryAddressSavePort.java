package com.tastyhouse.application.member.port.out.write;

import com.tastyhouse.domain.member.model.MemberDeliveryAddress;

public interface MemberDeliveryAddressSavePort {

    MemberDeliveryAddress save(MemberDeliveryAddress memberDeliveryAddress);

    void deleteById(Long addressId);
}
