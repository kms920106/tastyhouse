package com.tastyhouse.infrastructure.persistence.member.persistence;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.member.model.MemberWithdrawal;
import com.tastyhouse.application.member.port.out.write.MemberWithdrawalPersistencePort;

@Repository
class MemberWithdrawalPersistenceAdapter implements MemberWithdrawalPersistencePort {

    private final MemberWithdrawalJpaRepository memberWithdrawalJpaRepository;

    public MemberWithdrawalPersistenceAdapter(MemberWithdrawalJpaRepository memberWithdrawalJpaRepository) {
        this.memberWithdrawalJpaRepository = memberWithdrawalJpaRepository;
    }

    @Override
    public MemberWithdrawal save(MemberWithdrawal memberWithdrawal) {
        MemberWithdrawalJpaEntity saved = memberWithdrawalJpaRepository.save(MemberWithdrawalMapper.toEntity(memberWithdrawal));
        return MemberWithdrawalMapper.toDomain(saved);
    }
}
