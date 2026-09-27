package com.tastyhouse.application.member.port.out;

import java.util.List;
import java.util.Optional;

public interface MemberDeliveryAddressQueryPort {

    List<MemberDeliveryAddressItemResult> findByMemberId(Long memberId);

    Optional<Long> findDefaultAdminDongId(Long memberId);
}
