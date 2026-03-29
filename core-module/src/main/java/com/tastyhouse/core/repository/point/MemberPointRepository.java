package com.tastyhouse.core.repository.point;

import com.tastyhouse.core.entity.point.MemberPoint;
import com.tastyhouse.core.entity.point.MemberPointHistory;

import java.util.List;
import java.util.Optional;

public interface MemberPointRepository {

    Optional<MemberPoint> findByMemberId(Long memberId);

    List<MemberPointHistory> findPointHistoryByMemberIdOrderByCreatedAtDesc(Long memberId);

    MemberPoint save(MemberPoint memberPoint);

    MemberPointHistory saveHistory(MemberPointHistory memberPointHistory);
}
