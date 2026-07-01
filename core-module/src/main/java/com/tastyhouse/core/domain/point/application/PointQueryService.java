package com.tastyhouse.core.domain.point.application;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.point.application.dto.result.MemberPointHistoryResult;
import com.tastyhouse.core.domain.point.application.dto.result.MemberPointResult;
import com.tastyhouse.core.domain.point.domain.repository.MemberPointHistoryRepository;
import com.tastyhouse.core.domain.point.domain.repository.MemberPointRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PointQueryService {

    private final MemberPointRepository memberPointRepository;
    private final MemberPointHistoryRepository memberPointHistoryRepository;

    public Optional<MemberPointResult> findMemberPoint(Long memberId) {
        return memberPointRepository.findByMemberId(memberId)
            .map(MemberPointResult::from);
    }

    public List<MemberPointHistoryResult> findPointHistory(Long memberId) {
        return memberPointHistoryRepository.findByMemberIdOrderByCreatedAtDesc(memberId)
            .stream()
            .map(MemberPointHistoryResult::from)
            .collect(Collectors.toList());
    }
}
