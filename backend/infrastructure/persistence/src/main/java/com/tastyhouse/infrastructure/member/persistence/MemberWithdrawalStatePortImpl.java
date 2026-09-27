package com.tastyhouse.infrastructure.member.persistence;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.member.port.out.write.MemberWithdrawalState;
import com.tastyhouse.application.member.port.out.write.MemberWithdrawalStatePort;

@Repository
public class MemberWithdrawalStatePortImpl implements MemberWithdrawalStatePort {
    private final MemberWithdrawalJpaRepository memberWithdrawalJpaRepository;

    public MemberWithdrawalStatePortImpl(MemberWithdrawalJpaRepository memberWithdrawalJpaRepository) {
        this.memberWithdrawalJpaRepository = memberWithdrawalJpaRepository;
    }

    @Override
    public MemberWithdrawalState save(MemberWithdrawalState state) {
        MemberWithdrawalJpaEntity saved = memberWithdrawalJpaRepository.save(MemberWithdrawalMapper.toEntity(state));
        return MemberWithdrawalMapper.toState(saved);
    }
}
