package com.tastyhouse.application.member.port.out.write;

import com.tastyhouse.domain.member.model.MemberWithdrawal;

public interface MemberWithdrawalRepository {

    MemberWithdrawal save(MemberWithdrawal memberWithdrawal);
}
