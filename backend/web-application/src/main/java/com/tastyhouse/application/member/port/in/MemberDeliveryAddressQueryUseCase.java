package com.tastyhouse.application.member.port.in;

import java.util.List;

import com.tastyhouse.application.member.port.out.MemberDeliveryAddressItemResult;

public interface MemberDeliveryAddressQueryUseCase {

    List<MemberDeliveryAddressItemResult> getMyDeliveryAddresses(Long memberId);
}
