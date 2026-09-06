package com.tastyhouse.application.member.port.in;

import com.tastyhouse.application.shared.marker.WebApp;
import java.util.List;

import com.tastyhouse.application.member.port.out.MemberDeliveryAddressItemResult;

@WebApp
public interface MemberDeliveryAddressQueryUseCase {

    List<MemberDeliveryAddressItemResult> getMyDeliveryAddresses(Long memberId);
}
