package com.tastyhouse.application.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.model.MemberStatus;
import com.tastyhouse.application.member.port.in.MemberPhoneAvailabilityQueryUseCase;
import com.tastyhouse.application.member.port.out.MemberQueryPort;

@Service
@Transactional(readOnly = true)
class MemberPhoneAvailabilityQueryService implements MemberPhoneAvailabilityQueryUseCase {

    private final MemberQueryPort memberQueryPort;

    public MemberPhoneAvailabilityQueryService(MemberQueryPort memberQueryPort) {
        this.memberQueryPort = memberQueryPort;
    }

    @Override
    public boolean checkPhoneAvailability(String phoneNumber) {
        return !memberQueryPort.existsByPhoneNumberAndStatusNot(phoneNumber, MemberStatus.DELETED.name());
    }
}
