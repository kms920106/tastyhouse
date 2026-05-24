package com.tastyhouse.core.domain.member.infrastructure.persistence;

import com.tastyhouse.core.domain.member.domain.model.MemberWithdrawal;
import com.tastyhouse.core.domain.member.domain.repository.MemberWithdrawalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberWithdrawalRepositoryImpl implements MemberWithdrawalRepository {

    private final MemberWithdrawalJpaRepository memberWithdrawalJpaRepository;

    @Override
    public MemberWithdrawal save(MemberWithdrawal memberWithdrawal) {
        return memberWithdrawalJpaRepository.save(memberWithdrawal);
    }
}
