package com.tastyhouse.core.domain.point.domain.repository;

import java.util.List;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.point.application.dto.PointSearchCondition;
import com.tastyhouse.core.domain.point.domain.model.PointHistory;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

public interface PointHistoryRepository {

    List<PointHistory> findByMemberIdOrderByCreatedAtDesc(MemberId memberId);

    PageResult<PointHistory> findPointHistory(PointSearchCondition condition, PageQuery pageQuery);

    PointHistory save(PointHistory history);
}
