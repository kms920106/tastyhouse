package com.tastyhouse.core.domain.member.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.member.domain.model.MemberWithdrawal;
import com.tastyhouse.core.domain.member.domain.repository.MemberWithdrawalRepository;

@Repository
@RequiredArgsConstructor
public class MemberWithdrawalRepositoryImpl implements MemberWithdrawalRepository {

    private final MemberWithdrawalJpaRepository memberWithdrawalJpaRepository;

    @Override
    public MemberWithdrawal save(MemberWithdrawal memberWithdrawal) {
        return memberWithdrawalJpaRepository.save(memberWithdrawal);
    }
}
