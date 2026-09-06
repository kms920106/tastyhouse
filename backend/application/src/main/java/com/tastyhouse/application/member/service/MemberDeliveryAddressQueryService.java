package com.tastyhouse.application.member.service;

import com.tastyhouse.application.shared.marker.WebApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.member.port.out.MemberDeliveryAddressItemResult;
import com.tastyhouse.application.member.port.out.MemberDeliveryAddressQueryPort;
import com.tastyhouse.application.member.port.in.MemberDeliveryAddressQueryUseCase;

@Service
@WebApp
@Transactional(readOnly = true)
public class MemberDeliveryAddressQueryService implements MemberDeliveryAddressQueryUseCase {

    private final MemberDeliveryAddressQueryPort memberDeliveryAddressQueryPort;

    public MemberDeliveryAddressQueryService(MemberDeliveryAddressQueryPort memberDeliveryAddressQueryPort) {
        this.memberDeliveryAddressQueryPort = memberDeliveryAddressQueryPort;
    }

    @Override
    public List<MemberDeliveryAddressItemResult> getMyDeliveryAddresses(Long memberId) {
        return memberDeliveryAddressQueryPort.findByMemberId(MemberId.of(memberId));
    }
}
