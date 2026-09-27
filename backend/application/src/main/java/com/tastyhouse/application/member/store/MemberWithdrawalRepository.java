package com.tastyhouse.application.member.store;

import com.tastyhouse.domain.member.model.MemberWithdrawal;

public interface MemberWithdrawalRepository {

    MemberWithdrawal save(MemberWithdrawal memberWithdrawal);
}
