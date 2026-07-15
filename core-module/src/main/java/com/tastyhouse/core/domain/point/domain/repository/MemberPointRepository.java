package com.tastyhouse.core.domain.point.domain.repository;

import java.util.Optional;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.point.domain.model.MemberPoint;

public interface MemberPointRepository {

    Optional<MemberPoint> findByMemberId(MemberId memberId);

    MemberPoint save(MemberPoint memberPoint);
}
