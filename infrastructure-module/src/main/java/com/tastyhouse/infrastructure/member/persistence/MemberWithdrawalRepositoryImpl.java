package com.tastyhouse.infrastructure.member.persistence;

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
        MemberWithdrawalJpaEntity saved = memberWithdrawalJpaRepository.save(MemberWithdrawalMapper.toEntity(memberWithdrawal));
        return MemberWithdrawalMapper.toDomain(saved);
    }
}
