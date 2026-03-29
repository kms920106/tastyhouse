package com.tastyhouse.core.repository.member;

import com.tastyhouse.core.entity.user.MemberWithdrawal;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberWithdrawalRepositoryImpl implements MemberWithdrawalRepository {

    private final EntityManager entityManager;

    @Override
    public MemberWithdrawal save(MemberWithdrawal memberWithdrawal) {
        entityManager.persist(memberWithdrawal);
        return memberWithdrawal;
    }
}
