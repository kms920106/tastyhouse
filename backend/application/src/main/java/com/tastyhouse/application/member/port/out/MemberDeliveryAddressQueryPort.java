package com.tastyhouse.application.member.port.out;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.member.vo.MemberId;

public interface MemberDeliveryAddressQueryPort {

    List<MemberDeliveryAddressItemResult> findByMemberId(MemberId memberId);

    Optional<Long> findDefaultAdminDongId(MemberId memberId);
}
