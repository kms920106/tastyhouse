package com.tastyhouse.core.domain.point.domain.repository;

import com.tastyhouse.core.domain.point.domain.model.MemberPoint;

import java.util.Optional;

public interface MemberPointRepository {

    Optional<MemberPoint> findByMemberId(Long memberId);

    MemberPoint save(MemberPoint memberPoint);
}
