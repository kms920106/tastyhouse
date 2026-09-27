package com.tastyhouse.application.member.store;

import com.tastyhouse.application.member.port.out.write.MemberWithdrawalStatePort;
import com.tastyhouse.domain.member.model.MemberWithdrawal;

public class MemberWithdrawalStore implements MemberWithdrawalRepository {
    private final MemberWithdrawalStatePort memberWithdrawalStatePort;

    public MemberWithdrawalStore(MemberWithdrawalStatePort memberWithdrawalStatePort) {
        this.memberWithdrawalStatePort = memberWithdrawalStatePort;
    }

    @Override
    public MemberWithdrawal save(MemberWithdrawal memberWithdrawal) {
        return MemberWithdrawalStateMapper.toDomain(
            memberWithdrawalStatePort.save(MemberWithdrawalStateMapper.toState(memberWithdrawal))
        );
    }
}
