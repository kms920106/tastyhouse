package com.tastyhouse.application.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.member.port.in.MemberNicknameAvailabilityQueryUseCase;
import com.tastyhouse.application.member.port.out.MemberQueryPort;

@Service
@Transactional(readOnly = true)
class MemberNicknameAvailabilityQueryService implements MemberNicknameAvailabilityQueryUseCase {

    private final MemberQueryPort memberQueryPort;

    public MemberNicknameAvailabilityQueryService(MemberQueryPort memberQueryPort) {
        this.memberQueryPort = memberQueryPort;
    }

    @Override
    public boolean checkNicknameAvailability(String nickname) {
        return !memberQueryPort.existsByNickname(nickname);
    }
}
