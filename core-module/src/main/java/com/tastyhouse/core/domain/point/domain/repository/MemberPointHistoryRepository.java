package com.tastyhouse.core.domain.point.domain.repository;

import java.util.List;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.point.application.dto.PointSearchCondition;
import com.tastyhouse.core.domain.point.domain.model.MemberPointHistory;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

public interface MemberPointHistoryRepository {

    List<MemberPointHistory> findByMemberIdOrderByCreatedAtDesc(MemberId memberId);

    PageResult<MemberPointHistory> findPointHistory(PointSearchCondition condition, PageQuery pageQuery);

    MemberPointHistory save(MemberPointHistory history);
}
