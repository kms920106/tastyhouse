package com.tastyhouse.application.member.port.out.write;

import com.tastyhouse.domain.member.model.MemberWithdrawal;

public interface MemberWithdrawalSavePort {

    MemberWithdrawal save(MemberWithdrawal memberWithdrawal);
}
