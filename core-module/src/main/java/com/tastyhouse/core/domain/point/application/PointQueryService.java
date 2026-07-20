package com.tastyhouse.core.domain.point.application;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.point.domain.repository.PointHistoryRepository;
import com.tastyhouse.core.domain.point.domain.repository.PointRepository;
import com.tastyhouse.core.domain.point.application.dto.PointSearchCondition;
import com.tastyhouse.core.domain.point.application.dto.result.PointHistoryResult;
import com.tastyhouse.core.domain.point.application.dto.result.PointResult;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PointQueryService {

    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;

    public Optional<PointResult> findMemberPoint(MemberId memberId) {
        return pointRepository.findByMemberId(memberId)
            .map(PointResult::from);
    }

    public List<PointHistoryResult> findPointHistory(MemberId memberId) {
        return pointHistoryRepository.findByMemberIdOrderByCreatedAtDesc(memberId)
            .stream()
            .map(PointHistoryResult::from)
            .collect(Collectors.toList());
    }

    public PageResult<PointHistoryResult> findPointHistory(PointSearchCondition condition, int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return pointHistoryRepository.findPointHistory(condition, pageQuery)
            .map(PointHistoryResult::from);
    }
}
