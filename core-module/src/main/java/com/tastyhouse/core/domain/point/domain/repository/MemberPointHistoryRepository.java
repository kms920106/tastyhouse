package com.tastyhouse.core.domain.point.domain.repository;

import com.tastyhouse.core.domain.point.domain.model.MemberPointHistory;

import java.util.List;

public interface MemberPointHistoryRepository {

    List<MemberPointHistory> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    MemberPointHistory save(MemberPointHistory history);
}
