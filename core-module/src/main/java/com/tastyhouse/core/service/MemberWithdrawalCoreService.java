package com.tastyhouse.core.service;

import com.tastyhouse.core.entity.user.MemberWithdrawal;
import com.tastyhouse.core.repository.member.MemberWithdrawalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberWithdrawalCoreService {

    private final MemberWithdrawalRepository memberWithdrawalRepository;

    @Transactional
    public MemberWithdrawal save(MemberWithdrawal memberWithdrawal) {
        return memberWithdrawalRepository.save(memberWithdrawal);
    }
}
