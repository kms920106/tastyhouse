package com.tastyhouse.infrastructure.member.persistence;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.member.model.MemberWithdrawal;
import com.tastyhouse.domain.member.repository.MemberWithdrawalRepository;

@Repository
public class MemberWithdrawalRepositoryImpl implements MemberWithdrawalRepository {

    private final MemberWithdrawalJpaRepository memberWithdrawalJpaRepository;

    public MemberWithdrawalRepositoryImpl(MemberWithdrawalJpaRepository memberWithdrawalJpaRepository) {
        this.memberWithdrawalJpaRepository = memberWithdrawalJpaRepository;
    }

    @Override
    public MemberWithdrawal save(MemberWithdrawal memberWithdrawal) {
        MemberWithdrawalJpaEntity saved = memberWithdrawalJpaRepository.save(MemberWithdrawalMapper.toEntity(memberWithdrawal));
        return MemberWithdrawalMapper.toDomain(saved);
    }
}
