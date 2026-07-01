package com.tastyhouse.core.domain.point.domain.repository;

import java.util.List;

import com.tastyhouse.core.domain.point.domain.model.MemberPointHistory;

public interface MemberPointHistoryRepository {

    List<MemberPointHistory> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    MemberPointHistory save(MemberPointHistory history);
}
