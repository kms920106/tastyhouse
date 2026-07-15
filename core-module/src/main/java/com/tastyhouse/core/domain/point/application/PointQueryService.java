package com.tastyhouse.core.domain.point.application;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.point.domain.repository.MemberPointHistoryRepository;
import com.tastyhouse.core.domain.point.domain.repository.MemberPointRepository;
import com.tastyhouse.core.domain.point.application.dto.result.MemberPointHistoryResult;
import com.tastyhouse.core.domain.point.application.dto.result.MemberPointResult;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PointQueryService {

    private final MemberPointRepository memberPointRepository;
    private final MemberPointHistoryRepository memberPointHistoryRepository;

    public Optional<MemberPointResult> findMemberPoint(MemberId memberId) {
        return memberPointRepository.findByMemberId(memberId)
            .map(MemberPointResult::from);
    }

    public List<MemberPointHistoryResult> findPointHistory(MemberId memberId) {
        return memberPointHistoryRepository.findByMemberIdOrderByCreatedAtDesc(memberId)
            .stream()
            .map(MemberPointHistoryResult::from)
            .collect(Collectors.toList());
    }
}
